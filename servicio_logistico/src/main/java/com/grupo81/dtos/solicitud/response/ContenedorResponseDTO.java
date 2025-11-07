package com.grupo81.dtos.solicitud.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorResponseDTO {
    private UUID id;
    private String identificacion;
    private BigDecimal pesoKg;
    private BigDecimal volumenM3;
    private String estadoActual;
    private String ubicacionActualDireccion;
}