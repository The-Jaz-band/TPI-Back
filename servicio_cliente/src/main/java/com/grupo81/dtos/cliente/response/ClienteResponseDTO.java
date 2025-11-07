package com.grupo81.dtos.cliente.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDTO {
    private UUID id;
    private String nombre;
    private String email;
    private String telefono;
    private String empresa;
    private LocalDateTime fechaCreacion;
}