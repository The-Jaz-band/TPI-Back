package com.grupo81.servicio_tarifa.dtos.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCalculoDTO {
    private BigDecimal distanciaKm;
    private BigDecimal costoBaseKm;
    private BigDecimal valorLitroCombustible;
    private BigDecimal consumoLitrosTotal;
    private Integer cantidadTramos;
    private BigDecimal diasEstadia;
}