package com.grupo81.controller;

import com.grupo81.servicio_flota.dtos.request.*;
import com.grupo81.servicio_flota.dtos.response.*;
import com.grupo81.servicio_flota.services.CamionService;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/camiones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Camiones", description = "Gestión de flota de camiones")
@SecurityRequirement(name = "bearer-jwt")
public class CamionController {
    
    private final CamionService camionService;
    
    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Registrar nuevo camión")
    public ResponseEntity<CamionResponseDTO> crearCamion(
            @Valid @RequestBody CamionCreateRequestDTO request) {
        CamionResponseDTO response = camionService.crearCamion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener camión por ID")
    public ResponseEntity<CamionResponseDTO> obtenerCamion(@PathVariable UUID id) {
        CamionResponseDTO response = camionService.obtenerCamion(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar camiones disponibles con capacidad mínima")
    public ResponseEntity<List<CamionResponseDTO>> obtenerCamionesDisponibles(
            @RequestParam BigDecimal pesoMinimo,
            @RequestParam BigDecimal volumenMinimo) {
        List<CamionResponseDTO> response = camionService.obtenerCamionesDisponibles(pesoMinimo, volumenMinimo);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar todos los camiones")
    public ResponseEntity<List<CamionResponseDTO>> listarCamiones(
            @RequestParam(required = false) Boolean disponible) {
        List<CamionResponseDTO> response = disponible != null ?
            camionService.listarPorDisponibilidad(disponible) :
            camionService.listarCamiones();
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar datos del camión")
    public ResponseEntity<CamionResponseDTO> actualizarCamion(
            @PathVariable UUID id,
            @Valid @RequestBody CamionUpdateRequestDTO request) {
        CamionResponseDTO response = camionService.actualizarCamion(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar disponibilidad del camión")
    public ResponseEntity<Void> actualizarDisponibilidad(
            @PathVariable UUID id,
            @RequestParam boolean disponible) {
        camionService.actualizarDisponibilidad(id, disponible);
        return ResponseEntity.noContent().build();
    }
}