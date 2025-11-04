package com.grupo81.servicio_logistico.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.grupo81.servicio_logistico.dtos.request.UbicacionDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponseDTO {
    private UUID id;
    private String numero;
    private ContenedorResponseDTO contenedor;
    private UUID clienteId;
    private UbicacionDTO origen;
    private UbicacionDTO destino;
    private String estado;
    private BigDecimal costoEstimado;
    private BigDecimal tiempoEstimadoHoras;
    private BigDecimal costoFinal;
    private BigDecimal tiempoRealHoras;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntrega;
}