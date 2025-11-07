package com.grupo81.repository;

import com.grupo81.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CamionRepository extends JpaRepository<Camion, UUID> {
    
    Optional<Camion> findByDominio(String dominio);
    boolean existsByDominio(String dominio);
    
    @Query("SELECT c FROM Camion c WHERE c.disponible = true " +
           "AND c.capacidadPesoKg >= :pesoMinimo " +
           "AND c.capacidadVolumenM3 >= :volumenMinimo")
    List<Camion> findCamionesDisponibles(
        @Param("pesoMinimo") BigDecimal pesoMinimo,
        @Param("volumenMinimo") BigDecimal volumenMinimo
    );
    
    List<Camion> findByDisponible(boolean disponible);
}