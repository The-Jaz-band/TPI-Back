package com.grupo81.servicio_logistico.dtos.solicitud.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudSeguimientoResponseDTO {
    private UUID solicitudId;
    private String numero;
    private String estadoSolicitud;
    private ContenedorSeguimientoDTO contenedor;
    private TramoActualDTO tramoActual;
    private BigDecimal costoEstimado;
    private BigDecimal tiempoEstimadoHoras;
    private LocalDateTime fechaCreacion;
}