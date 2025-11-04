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
@Table(name = "solicitudes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String numero;
    
    @OneToOne
    @JoinColumn(name = "contenedor_id", nullable = false)
    private Contenedor contenedor;
    
    @Column(nullable = false)
    private UUID clienteId;
    
    @Column(nullable = false, length = 500)
    private String origenDireccion;
    
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal origenLatitud;
    
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal origenLongitud;
    
    @Column(nullable = false, length = 500)
    private String destinoDireccion;
    
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal destinoLatitud;
    
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal destinoLongitud;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoSolicitud estado;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal costoEstimado;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal tiempoEstimadoHoras;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal costoFinal;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal tiempoRealHoras;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
    
    @Column
    private LocalDateTime fechaEntrega;
    
    public enum EstadoSolicitud {
        BORRADOR,
        PROGRAMADA,
        EN_TRANSITO,
        ENTREGADA,
        CANCELADA
    }
}