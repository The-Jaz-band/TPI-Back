package com.grupo81.servicio_logistico.client.dto;

import java.math.BigDecimal;

public record CostoCalculadoDTO(
    BigDecimal costoTotal,
    BigDecimal costoTraslado,
    BigDecimal costoCombustible,
    BigDecimal costoEstadia,
    BigDecimal costoGestion
) {}