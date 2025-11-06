package com.grupo81.servicio_tarifa.dtos.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostoCalculadoDTO {
    private BigDecimal costoTotal;
    private BigDecimal costoTraslado;
    private BigDecimal costoCombustible;
    private BigDecimal costoEstadia;
    private BigDecimal costoGestion;
    private DetalleCalculoDTO detalle;
}
