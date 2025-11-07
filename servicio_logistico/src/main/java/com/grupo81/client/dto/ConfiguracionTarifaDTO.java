package com.grupo81.client.dto;

import java.math.BigDecimal;

public record ConfiguracionTarifaDTO(
    BigDecimal costoBaseKm,
    BigDecimal valorLitroCombustible,
    BigDecimal consumoPromedioLKm,
    BigDecimal costoGestionPorTramo,
    BigDecimal costoEstadiaDiario
) {}
