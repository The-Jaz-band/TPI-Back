package com.grupo81.dtos.ruta.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignarCamionRequestDTO {
    
    @NotNull(message = "El ID del camión es obligatorio")
    private UUID camionId;
}