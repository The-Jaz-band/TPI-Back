package com.grupo81.servicio_logistico.dtos.solicitud.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionDTO {
    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(max = 500)
    private String direccion;
    
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitud;
    
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitud;
}