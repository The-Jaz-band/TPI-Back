package com.grupo81.repository;

import com.grupo81.entity.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, UUID> {
    List<Deposito> findByActivoTrue();
    Optional<Deposito> findByIdAndActivoTrue(UUID id);
    boolean existsByNombreAndActivoTrue(String nombre);
}
