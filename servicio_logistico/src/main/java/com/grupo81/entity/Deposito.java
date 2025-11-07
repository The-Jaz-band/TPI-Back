package com.grupo81.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "depositos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deposito {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 200)
    private String nombre;
    
    @Column(nullable = false, length = 500)
    private String direccion;
    
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitud;
    
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitud;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEstadiaDiario;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
}