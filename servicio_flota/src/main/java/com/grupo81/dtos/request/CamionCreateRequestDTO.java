package com.grupo81.dtos.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CamionCreateRequestDTO {
    
    @NotBlank(message = "El dominio es obligatorio")
    @Size(max = 20)
    private String dominio;
    
    @NotBlank(message = "El nombre del transportista es obligatorio")
    @Size(max = 200)
    private String nombreTransportista;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20)
    private String telefonoTransportista;
    
    @NotNull(message = "La capacidad de peso es obligatoria")
    @DecimalMin(value = "0.01", message = "La capacidad debe ser mayor a 0")
    private BigDecimal capacidadPesoKg;
    
    @NotNull(message = "La capacidad de volumen es obligatoria")
    @DecimalMin(value = "0.01", message = "La capacidad debe ser mayor a 0")
    private BigDecimal capacidadVolumenM3;
    
    @NotNull(message = "El costo base por km es obligatorio")
    @DecimalMin(value = "0.0")
    private BigDecimal costoBaseKm;
    
    @NotNull(message = "El consumo de combustible es obligatorio")
    @DecimalMin(value = "0.01", message = "El consumo debe ser mayor a 0")
    private BigDecimal consumoCombustibleLKm;
}