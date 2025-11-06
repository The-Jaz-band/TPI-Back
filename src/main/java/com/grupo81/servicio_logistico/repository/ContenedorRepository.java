package com.grupo81.servicio_logistico.repository;

import com.grupo81.servicio_logistico.entity.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, UUID> {
    Optional<Contenedor> findByIdentificacion(String identificacion);
    boolean existsByIdentificacion(String identificacion);
    
    @Query("SELECT c FROM Contenedor c WHERE c.estadoActual = :estado")
    List<Contenedor> findByEstadoActual(@Param("estado") Contenedor.EstadoContenedor estado);
    
    @Query("SELECT c FROM Contenedor c WHERE c.clienteId = :clienteId")
    List<Contenedor> findByClienteId(@Param("clienteId") UUID clienteId);
}
