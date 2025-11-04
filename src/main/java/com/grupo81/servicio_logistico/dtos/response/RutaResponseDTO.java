package com.grupo81.servicio_logistico.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaResponseDTO {
    private UUID id;
    private UUID solicitudId;
    private Integer cantidadTramos;
    private Integer cantidadDepositos;
    private List<TramoResponseDTO> tramos;
    private LocalDateTime fechaCreacion;
}