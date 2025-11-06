package com.grupo81.servicio_logistico.repository;

import com.grupo81.servicio_logistico.entity.Tramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, UUID> {
    
    @Query("SELECT t FROM Tramo t WHERE t.ruta.id = :rutaId ORDER BY t.orden")
    List<Tramo> findByRutaIdOrderByOrden(@Param("rutaId") UUID rutaId);
    
    @Query("SELECT t FROM Tramo t WHERE t.camionId = :camionId AND t.estado IN :estados")
    List<Tramo> findByCamionIdAndEstadoIn(
        @Param("camionId") UUID camionId, 
        @Param("estados") List<Tramo.EstadoTramo> estados
    );
    
    @Query("SELECT t FROM Tramo t WHERE t.estado = :estado ORDER BY t.fechaCreacion")
    List<Tramo> findByEstado(@Param("estado") Tramo.EstadoTramo estado);
    
    @Query("SELECT t FROM Tramo t WHERE t.deposito.id = :depositoId AND t.estado = 'FINALIZADO' " +
           "AND NOT EXISTS (SELECT t2 FROM Tramo t2 WHERE t2.ruta.id = t.ruta.id AND t2.orden = t.orden + 1 AND t2.estado = 'INICIADO')")
    List<Tramo> findContenedoresEnDeposito(@Param("depositoId") UUID depositoId);
    
    @Query("SELECT t FROM Tramo t WHERE t.ruta.solicitud.contenedor.id = :contenedorId " +
           "AND t.estado IN ('ASIGNADO', 'INICIADO') ORDER BY t.orden")
    Optional<Tramo> findTramoActualByContenedorId(@Param("contenedorId") UUID contenedorId);
}
