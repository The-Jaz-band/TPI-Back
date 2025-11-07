package com.grupo81.controller;

import com.grupo81.dtos.ruta.request.*;
import com.grupo81.dtos.ruta.response.*;
import com.grupo81.services.RutaService;
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
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rutas", description = "Gestión de rutas y tramos de transporte")
@SecurityRequirement(name = "bearer-jwt")
public class RutaController {
    
    private final RutaService rutaService;
    
    @PostMapping("/tentativa")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Calcular ruta tentativa",
               description = "Consulta rutas tentativas con todos los tramos sugeridos, tiempo y costo estimados")
    public ResponseEntity<RutaTentativaResponseDTO> calcularRutaTentativa(
            @RequestParam UUID solicitudId,
            @RequestParam List<UUID> depositosIds) {
        log.info("Calculando ruta tentativa para solicitud: {}", solicitudId);
        RutaTentativaResponseDTO response = rutaService.calcularRutaTentativa(solicitudId, depositosIds);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Asignar ruta a una solicitud",
               description = "Asigna una ruta definitiva con todos sus tramos a una solicitud")
    public ResponseEntity<RutaResponseDTO> asignarRuta(
            @Valid @RequestBody RutaAsignacionRequestDTO request) {
        log.info("Asignando ruta a solicitud: {}", request.getSolicitudId());
        RutaResponseDTO response = rutaService.asignarRuta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/solicitud/{solicitudId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener ruta de una solicitud")
    public ResponseEntity<RutaResponseDTO> obtenerRutaPorSolicitud(@PathVariable UUID solicitudId) {
        // Implementación simplificada - deberías agregar este método en RutaService
        log.info("Consultando ruta de solicitud: {}", solicitudId);
        return ResponseEntity.ok(null); // TODO: Implementar
    }
}