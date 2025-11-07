package com.grupo81.dtos.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CamionUpdateRequestDTO {
    
    @Size(max = 200)
    private String nombreTransportista;
    
    @Size(max = 20)
    private String telefonoTransportista;
    
    @DecimalMin(value = "0.0")
    private BigDecimal costoBaseKm;
    
    @DecimalMin(value = "0.01")
    private BigDecimal consumoCombustibleLKm;
    
    private Boolean disponible;
}