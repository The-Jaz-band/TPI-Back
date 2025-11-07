package com.grupo81.controller;

import com.grupo81.dtos.request.*;
import com.grupo81.dtos.response.*;
import com.grupo81.services.TarifaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tarifas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tarifas", description = "Gestión de tarifas y cálculo de costos")
@SecurityRequirement(name = "bearer-jwt")
public class TarifaController {
    
    private final TarifaService tarifaService;
    
    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear nueva tarifa")
    public ResponseEntity<TarifaResponseDTO> crearTarifa(
            @Valid @RequestBody TarifaCreateRequestDTO request) {
        TarifaResponseDTO response = tarifaService.crearTarifa(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/configuracion")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener configuración actual de tarifas")
    public ResponseEntity<ConfiguracionTarifaDTO> obtenerConfiguracion() {
        ConfiguracionTarifaDTO response = tarifaService.obtenerConfiguracion();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/calcular-costo")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Calcular costo de un traslado")
    public ResponseEntity<CostoCalculadoDTO> calcularCosto(
            @Valid @RequestBody CalculoCostoRequestDTO request) {
        CostoCalculadoDTO response = tarifaService.calcularCosto(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar todas las tarifas")
    public ResponseEntity<List<TarifaResponseDTO>> listarTarifas() {
        List<TarifaResponseDTO> response = tarifaService.listarTarifas();
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar tarifa")
    public ResponseEntity<TarifaResponseDTO> actualizarTarifa(
            @PathVariable UUID id,
            @Valid @RequestBody TarifaUpdateRequestDTO request) {
        TarifaResponseDTO response = tarifaService.actualizarTarifa(id, request);
        return ResponseEntity.ok(response);
    }
}