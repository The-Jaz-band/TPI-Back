package com.grupo81.dtos.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaCreateRequestDTO {
    
    @NotBlank(message = "El código de tarifa es obligatorio")
    @Size(max = 50)
    private String codigoTarifa;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
    
    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.0")
    private BigDecimal valor;
    
    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidad; // POR_KM, POR_LITRO, POR_TRAMO, POR_DIA, FIJO
}
