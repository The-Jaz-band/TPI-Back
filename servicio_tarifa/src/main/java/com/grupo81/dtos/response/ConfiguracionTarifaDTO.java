package com.grupo81.dtos.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionTarifaDTO {
    private BigDecimal costoBaseKm;
    private BigDecimal valorLitroCombustible;
    private BigDecimal consumoPromedioLKm;
    private BigDecimal costoGestionPorTramo;
    private BigDecimal costoEstadiaDiario;
}