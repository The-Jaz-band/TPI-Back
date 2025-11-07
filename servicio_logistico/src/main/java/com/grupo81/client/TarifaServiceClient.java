package com.grupo81.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.grupo81.client.dto.*;


@FeignClient(
    name = "servicio-tarifa",
    url = "${microservices.tarifas.url}",
    configuration = FeignClientConfiguration.class
)
public interface TarifaServiceClient {
    
    @PostMapping("/api/tarifas/calcular-costo")
    CostoCalculadoDTO calcularCosto(@RequestBody CalculoCostoRequestDTO request);
    
    @GetMapping("/api/tarifas/configuracion")
    ConfiguracionTarifaDTO obtenerConfiguracion();
}

// DTOs para Tarifa
/*
record CalculoCostoRequestDTO(
    BigDecimal distanciaKm,
    BigDecimal pesoKg,
    BigDecimal volumenM3,
    Integer cantidadTramos,
    BigDecimal diasEstadia,
    UUID camionId
) {}

record CostoCalculadoDTO(
    BigDecimal costoTotal,
    BigDecimal costoTraslado,
    BigDecimal costoCombustible,
    BigDecimal costoEstadia,
    BigDecimal costoGestion
) {}

record ConfiguracionTarifaDTO(
    BigDecimal costoBaseKm,
    BigDecimal valorLitroCombustible,
    BigDecimal consumoPromedioLKm,
    BigDecimal costoGestionPorTramo
) {}
*/