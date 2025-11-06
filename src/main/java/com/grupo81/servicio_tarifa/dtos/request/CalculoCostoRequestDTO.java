package com.grupo81.servicio_tarifa.dtos.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculoCostoRequestDTO {
    
    @NotNull
    private BigDecimal distanciaKm;
    
    @NotNull
    private BigDecimal pesoKg;
    
    @NotNull
    private BigDecimal volumenM3;
    
    @NotNull
    @Min(1)
    private Integer cantidadTramos;
    
    private BigDecimal diasEstadia;
    
    private UUID camionId;
}