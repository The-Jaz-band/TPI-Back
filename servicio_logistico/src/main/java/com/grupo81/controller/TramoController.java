package com.grupo81.controller;

import com.grupo81.dtos.ruta.request.*;
import com.grupo81.dtos.ruta.response.*;
import com.grupo81.services.TramoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tramos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tramos", description = "Gestión de tramos de transporte")
@SecurityRequirement(name = "bearer-jwt")
public class TramoController {
    
    private final TramoService tramoService;
    
    @PutMapping("/{id}/asignar-camion")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Asignar camión a un tramo",
               description = "Permite al operador asignar un camión disponible a un tramo de traslado")
    public ResponseEntity<TramoResponseDTO> asignarCamion(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarCamionRequestDTO request) {
        log.info("Asignando camión al tramo: {}", id);
        TramoResponseDTO response = tramoService.asignarCamion(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    @Operation(summary = "Iniciar un tramo de traslado",
               description = "Permite al transportista registrar el inicio de un tramo asignado")
    public ResponseEntity<TramoResponseDTO> iniciarTramo(@PathVariable UUID id) {
        log.info("Iniciando tramo: {}", id);
        TramoResponseDTO response = tramoService.iniciarTramo(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    @Operation(summary = "Finalizar un tramo de traslado",
               description = "Permite al transportista registrar la finalización de un tramo")
    public ResponseEntity<TramoResponseDTO> finalizarTramo(@PathVariable UUID id) {
        log.info("Finalizando tramo: {}", id);
        TramoResponseDTO response = tramoService.finalizarTramo(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/camion/{camionId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener tramos asignados a un camión",
               description = "Lista los tramos asignados o en curso para un camión específico")
    public ResponseEntity<List<TramoResponseDTO>> obtenerTramosPorCamion(@PathVariable UUID camionId) {
        log.info("Consultando tramos del camión: {}", camionId);
        List<TramoResponseDTO> response = tramoService.obtenerTramosPorCamion(camionId);
        return ResponseEntity.ok(response);
    }
}