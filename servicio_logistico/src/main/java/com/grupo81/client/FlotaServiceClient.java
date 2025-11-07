package com.grupo81.client;

import org.springframework.cloud.openfeign.FeignClient;  // ← AGREGAR ESTE IMPORT
import org.springframework.web.bind.annotation.*;

import com.grupo81.client.dto.CamionDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FeignClient(
    name = "servicio-flota",
    url = "${microservices.flota.url}",
    configuration = FeignClientConfiguration.class
)
public interface FlotaServiceClient {
    
    @GetMapping("/api/camiones/{id}")
    CamionDTO obtenerCamion(@PathVariable("id") UUID id);
    
    @GetMapping("/api/camiones/disponibles")
    List<CamionDTO> obtenerCamionesDisponibles(
        @RequestParam("pesoMinimo") BigDecimal pesoMinimo,
        @RequestParam("volumenMinimo") BigDecimal volumenMinimo
    );
    
    @PutMapping("/api/camiones/{id}/disponibilidad")
    void actualizarDisponibilidad(
        @PathVariable("id") UUID id,
        @RequestParam("disponible") boolean disponible
    );
}