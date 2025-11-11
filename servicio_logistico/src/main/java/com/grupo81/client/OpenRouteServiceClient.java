package com.grupo81.client;

import com.grupo81.client.dto.openrouteservice.OrsDirectionsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "openrouteservice",
    url = "${microservices.openrouteservice.base-url}"
)
public interface OpenRouteServiceClient {
    
    @GetMapping("/v2/directions/driving-car")
    OrsDirectionsResponse obtenerDirecciones(
        @RequestParam("api_key") String apiKey,
        @RequestParam("start") String start, // Formato: "longitud,latitud"
        @RequestParam("end") String end      // Formato: "longitud,latitud"
    );
}