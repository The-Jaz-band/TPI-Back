package com.grupo81.dtos.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaResponseDTO {
    private UUID id;
    private String codigoTarifa;
    private String descripcion;
    private BigDecimal valor;
    private String unidad;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}