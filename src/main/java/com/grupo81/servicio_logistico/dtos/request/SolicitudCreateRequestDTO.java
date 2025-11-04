package com.grupo81.servicio_logistico.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCreateRequestDTO {
    
    @Valid
    @NotNull(message = "El contenedor es obligatorio")
    private ContenedorRequestDTO contenedor;
    
    @Valid
    @NotNull(message = "Los datos del cliente son obligatorios")
    private ClienteRequestDTO cliente;
    
    @Valid
    @NotNull(message = "El origen es obligatorio")
    private UbicacionDTO origen;
    
    @Valid
    @NotNull(message = "El destino es obligatorio")
    private UbicacionDTO destino;
}