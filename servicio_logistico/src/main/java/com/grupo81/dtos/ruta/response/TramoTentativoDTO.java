package com.grupo81.dtos.ruta.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoTentativoDTO {
    private Integer orden;
    private String tipo;
    private String origenDireccion;
    private String destinoDireccion;
    private BigDecimal distanciaKm;
    private BigDecimal costoEstimado;
    private BigDecimal tiempoEstimadoHoras;
    private UUID depositoId;
    private String depositoNombre;
}