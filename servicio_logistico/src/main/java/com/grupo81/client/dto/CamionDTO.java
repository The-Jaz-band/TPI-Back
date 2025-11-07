package com.grupo81.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CamionDTO(
    UUID id,
    String dominio,
    String nombreTransportista,
    String telefonoTransportista,
    BigDecimal capacidadPesoKg,
    BigDecimal capacidadVolumenM3,
    BigDecimal costoBaseKm,
    BigDecimal consumoCombustibleLKm,
    boolean disponible
) {}
