package com.grupo81.servicio_logistico.dtos.deposito.request;

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
public class DepositoUpdateRequestDTO {
    
    @Size(max = 200)
    private String nombre;
    
    @Size(max = 500)
    private String direccion;
    
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitud;
    
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitud;
    
    @DecimalMin(value = "0.0", message = "El costo debe ser mayor o igual a 0")
    private BigDecimal costoEstadiaDiario;
    
    private Boolean activo;
}