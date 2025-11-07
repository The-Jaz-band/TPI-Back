package com.grupo81.servicio_logistico.controller;

import com.grupo81.servicio_logistico.dtos.solicitud.*;
import com.grupo81.servicio_logistico.dtos.solicitud.request.SolicitudCreateRequestDTO;
import com.grupo81.servicio_logistico.dtos.solicitud.response.SolicitudResponseDTO;
import com.grupo81.servicio_logistico.dtos.solicitud.response.SolicitudSeguimientoResponseDTO;
import com.grupo81.servicio_logistico.services.SolicitudService;
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
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Solicitudes", description = "Gestión de solicitudes de transporte")
@SecurityRequirement(name = "bearer-jwt")
public class SolicitudController {
    
    private final SolicitudService solicitudService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
    @Operation(summary = "Crear nueva solicitud de transporte", 
               description = "Permite a un cliente registrar una nueva solicitud de transporte de contenedor")
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(
            @Valid @RequestBody SolicitudCreateRequestDTO request) {
        log.info("Recibida solicitud de creación de transporte");
        SolicitudResponseDTO response = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener solicitud por ID",
               description = "Consulta los detalles de una solicitud específica")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitud(@PathVariable UUID id) {
        log.info("Consultando solicitud con ID: {}", id);
        SolicitudResponseDTO response = solicitudService.obtenerSolicitud(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/seguimiento")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
    @Operation(summary = "Consultar estado del transporte",
               description = "Permite al cliente realizar seguimiento del estado de su contenedor")
    public ResponseEntity<SolicitudSeguimientoResponseDTO> obtenerSeguimiento(@PathVariable UUID id) {
        log.info("Consultando seguimiento de solicitud: {}", id);
        SolicitudSeguimientoResponseDTO response = solicitudService.obtenerSeguimiento(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
    @Operation(summary = "Listar solicitudes por cliente",
               description = "Obtiene todas las solicitudes asociadas a un cliente")
    public ResponseEntity<List<SolicitudResponseDTO>> listarSolicitudesPorCliente(
            @PathVariable UUID clienteId) {
        log.info("Listando solicitudes del cliente: {}", clienteId);
        List<SolicitudResponseDTO> response = solicitudService.listarSolicitudesPorCliente(clienteId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar solicitudes pendientes",
               description = "Consulta todas las solicitudes que están pendientes de entrega")
    public ResponseEntity<List<SolicitudResponseDTO>> listarSolicitudesPendientes() {
        log.info("Listando solicitudes pendientes");
        List<SolicitudResponseDTO> response = solicitudService.listarSolicitudesPendientes();
        return ResponseEntity.ok(response);
    }
}