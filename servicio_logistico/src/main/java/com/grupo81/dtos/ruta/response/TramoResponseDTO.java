package com.grupo81.dtos.ruta.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TramoResponseDTO {
    private UUID id;
    private Integer orden;
    private String tipo;
    private String estado;
    private String origenDireccion;
    private BigDecimal origenLatitud;
    private BigDecimal origenLongitud;
    private String destinoDireccion;
    private BigDecimal destinoLatitud;
    private BigDecimal destinoLongitud;
    private BigDecimal distanciaKm;
    private BigDecimal costoAproximado;
    private BigDecimal costoReal;
    private LocalDateTime fechaHoraEstimadaInicio;
    private LocalDateTime fechaHoraEstimadaFin;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private UUID camionId;
    private UUID depositoId;
}
