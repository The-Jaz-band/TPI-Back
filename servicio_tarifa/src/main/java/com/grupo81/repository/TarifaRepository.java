package com.grupo81.repository;

import com.grupo81.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, UUID> {
    Optional<Tarifa> findByCodigoTarifa(String codigoTarifa);
    boolean existsByCodigoTarifa(String codigoTarifa);
}