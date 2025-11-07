package com.grupo81.dtos.ruta.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorPendienteResponseDTO {
    private UUID contenedorId;
    private String identificacion;
    private UUID solicitudId;
    private String numeroSolicitud;
    private String estadoContenedor;
    private String ubicacionActual;
    private UUID clienteId;
    private LocalDateTime fechaSolicitud;
}
