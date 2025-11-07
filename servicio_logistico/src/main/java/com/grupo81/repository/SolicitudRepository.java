package com.grupo81.repository;

import com.grupo81.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, UUID> {
    Optional<Solicitud> findByNumero(String numero);
    
    @Query("SELECT s FROM Solicitud s WHERE s.clienteId = :clienteId ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findByClienteId(@Param("clienteId") UUID clienteId);
    
    @Query("SELECT s FROM Solicitud s WHERE s.estado = :estado ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findByEstado(@Param("estado") Solicitud.EstadoSolicitud estado);
    
    @Query("SELECT s FROM Solicitud s WHERE s.estado IN :estados ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findByEstadoIn(@Param("estados") List<Solicitud.EstadoSolicitud> estados);
    
    @Query("SELECT COUNT(s) FROM Solicitud s WHERE FUNCTION('DATE', s.fechaCreacion) = CURRENT_DATE")
    Long countSolicitudesToday();
}