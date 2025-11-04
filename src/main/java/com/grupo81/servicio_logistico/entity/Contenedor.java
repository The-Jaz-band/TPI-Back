package com.grupo81.servicio_logistico.entity;

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
@Table(name = "contenedores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contenedor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String identificacion;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoKg;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volumenM3;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoContenedor estadoActual;
    
    @Column(nullable = false, length = 500)
    private String ubicacionActualDireccion;
    
    @Column(nullable = false)
    private UUID clienteId;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
    
    public enum EstadoContenedor {
        EN_ORIGEN,
        RETIRADO,
        EN_VIAJE,
        EN_DEPOSITO,
        ENTREGADO
    }
}