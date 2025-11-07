package com.grupo81.dtos.cliente.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteUpdateRequestDTO {
    
    @Size(max = 200)
    private String nombre;
    
    @Size(max = 20)
    private String telefono;
    
    @Size(max = 200)
    private String empresa;
}