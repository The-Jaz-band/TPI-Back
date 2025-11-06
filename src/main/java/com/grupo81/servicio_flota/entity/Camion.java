package com.grupo81.servicio_flota.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "camiones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Camion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String dominio;
    
    @Column(nullable = false, length = 200)
    private String nombreTransportista;
    
    @Column(nullable = false, length = 20)
    private String telefonoTransportista;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPesoKg;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumenM3;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costoBaseKm;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consumoCombustibleLKm;
    
    @Column(nullable = false)
    private Boolean disponible = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
}