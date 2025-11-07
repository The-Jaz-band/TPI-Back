package com.grupo81.repository;

import com.grupo81.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, UUID> {
    Optional<Ruta> findBySolicitudId(UUID solicitudId);
    
    @Query("SELECT r FROM Ruta r JOIN FETCH r.tramos WHERE r.solicitud.id = :solicitudId")
    Optional<Ruta> findBySolicitudIdWithTramos(@Param("solicitudId") UUID solicitudId);
}
