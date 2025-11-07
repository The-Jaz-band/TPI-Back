package com.grupo81.dtos.deposito.response;

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
public class ContenedorEnDepositoResponseDTO {
    private UUID contenedorId;
    private String identificacion;
    private UUID solicitudId;
    private String numeroSolicitud;
    private UUID depositoId;
    private String depositoNombre;
    private LocalDateTime fechaIngreso;
    private UUID proximoTramoId;
    private String proximoDestino;
    private Boolean camionAsignado;
}
