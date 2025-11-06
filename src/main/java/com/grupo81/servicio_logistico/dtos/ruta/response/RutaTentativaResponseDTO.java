package com.grupo81.servicio_logistico.dtos.ruta.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaTentativaResponseDTO {
    private UUID solicitudId;
    private List<TramoTentativoDTO> tramos;
    private BigDecimal costoEstimadoTotal;
    private BigDecimal tiempoEstimadoTotalHoras;
    private BigDecimal distanciaTotalKm;
}