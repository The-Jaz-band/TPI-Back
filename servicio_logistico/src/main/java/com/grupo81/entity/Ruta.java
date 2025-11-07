package com.grupo81.servicio_logistico.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rutas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ruta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @OneToOne
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    private Solicitud solicitud;
    
    @Column(nullable = false)
    private Integer cantidadTramos;
    
    @Column(nullable = false)
    private Integer cantidadDepositos;
    
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Tramo> tramos = new ArrayList<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
    
    public void addTramo(Tramo tramo) {
        tramos.add(tramo);
        tramo.setRuta(this);
    }
    
    public void removeTramo(Tramo tramo) {
        tramos.remove(tramo);
        tramo.setRuta(null);
    }
}