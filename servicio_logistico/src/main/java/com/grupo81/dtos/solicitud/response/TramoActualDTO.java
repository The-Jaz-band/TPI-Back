package com.grupo81.dtos.solicitud.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TramoActualDTO {
    private UUID tramoId;
    private String tipo;
    private String estado;
    private String origenDireccion;
    private String destinoDireccion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaEstimadaFin;
}