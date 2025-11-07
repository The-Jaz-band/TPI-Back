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
@Table(name = "tramos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tramo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;
    
    @Column(nullable = false)
    private Integer orden;
    
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
    private TipoTramo tipo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoTramo estado;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal distanciaKm;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal costoAproximado;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal costoReal;
    
    @Column
    private LocalDateTime fechaHoraEstimadaInicio;
    
    @Column
    private LocalDateTime fechaHoraEstimadaFin;
    
    @Column
    private LocalDateTime fechaHoraInicio;
    
    @Column
    private LocalDateTime fechaHoraFin;
    
    @Column
    private UUID camionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposito_id")
    private Deposito deposito;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
    
    public enum TipoTramo {
        ORIGEN_DEPOSITO,
        DEPOSITO_DEPOSITO,
        DEPOSITO_DESTINO,
        ORIGEN_DESTINO
    }
    
    public enum EstadoTramo {
        ESTIMADO,
        ASIGNADO,
        INICIADO,
        FINALIZADO,
        CANCELADO
    }
}