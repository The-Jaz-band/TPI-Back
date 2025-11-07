package com.grupo81.servicio_tarifa.dtos.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaUpdateRequestDTO {
    
    private String descripcion;
    
    @DecimalMin(value = "0.0")
    private BigDecimal valor;
}