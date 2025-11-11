package com.grupo81.client.dto.geoapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta del servicio GeoAPI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanciaDTO {
    private String origen;
    private String destino;
    private double kilometros;
    private String duracionTexto;
    private long duracionSegundos;
}