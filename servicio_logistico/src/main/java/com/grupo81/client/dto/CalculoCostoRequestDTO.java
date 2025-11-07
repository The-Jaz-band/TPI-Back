package com.grupo81.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CalculoCostoRequestDTO(
    BigDecimal distanciaKm,
    BigDecimal pesoKg,
    BigDecimal volumenM3,
    Integer cantidadTramos,
    BigDecimal diasEstadia,
    UUID camionId
) {}
