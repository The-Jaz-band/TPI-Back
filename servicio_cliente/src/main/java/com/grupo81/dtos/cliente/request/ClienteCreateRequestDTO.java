package com.grupo81.dtos.cliente.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteCreateRequestDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 200)
    private String email;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20)
    private String telefono;
    
    @Size(max = 200)
    private String empresa;
}