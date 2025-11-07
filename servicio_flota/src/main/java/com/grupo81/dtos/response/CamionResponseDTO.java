package com.grupo81.servicio_flota.dtos.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CamionResponseDTO {
    private UUID id;
    private String dominio;
    private String nombreTransportista;
    private String telefonoTransportista;
    private BigDecimal capacidadPesoKg;
    private BigDecimal capacidadVolumenM3;
    private BigDecimal costoBaseKm;
    private BigDecimal consumoCombustibleLKm;
    private Boolean disponible;
    private LocalDateTime fechaCreacion;
}