package com.grupo81.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.grupo81.client.dto.geoapi.DistanciaDTO;

/**
 * Cliente Feign para el servicio GeoAPI
 * Calcula distancias usando Google Maps Distance Matrix API
 */
@FeignClient(
    name = "servicio-geoapi",
    url = "${microservices.geoapi.url}"
)
public interface GeoApiClient {
    
    @GetMapping("/api/distancia")
    DistanciaDTO calcularDistancia(
        @RequestParam("origen") String origen,
        @RequestParam("destino") String destino
    );
}