package com.grupo81.servicio_logistico.client;

import org.springframework.web.bind.annotation.*;

import com.grupo81.servicio_logistico.client.dto.CamionDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

// DTOs para Camión
/*record CamionDTO(
    UUID id,
    String dominio,
    String nombreTransportista,
    String telefonoTransportista,
    BigDecimal capacidadPesoKg,
    BigDecimal capacidadVolumenM3,
    BigDecimal costoBaseKm,
    BigDecimal consumoCombustibleLKm,
    boolean disponible
) {}
*/