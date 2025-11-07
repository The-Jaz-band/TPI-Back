package com.grupo81.dtos.ruta.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaAsignacionRequestDTO {
    
    @NotNull(message = "El ID de solicitud es obligatorio")
    private UUID solicitudId;
    
    @NotEmpty(message = "Debe incluir al menos un depósito")
    private List<UUID> depositosIds;
}