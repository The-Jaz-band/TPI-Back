package com.grupo81.controller;

import com.grupo81.dtos.deposito.request.*;
import com.grupo81.dtos.deposito.response.ContenedorEnDepositoResponseDTO;
import com.grupo81.dtos.deposito.response.DepositoResponseDTO;
import com.grupo81.services.DepositoService;
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
@RequestMapping("/api/depositos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Depósitos", description = "Gestión de depósitos")
@SecurityRequirement(name = "bearer-jwt")
public class DepositoController {
    
    private final DepositoService depositoService;
    
    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear nuevo depósito",
               description = "Permite registrar un nuevo punto de almacenamiento temporal")
    public ResponseEntity<DepositoResponseDTO> crearDeposito(
            @Valid @RequestBody DepositoCreateRequestDTO request) {
        log.info("Creando depósito");
        DepositoResponseDTO response = depositoService.crearDeposito(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Obtener depósito por ID")
    public ResponseEntity<DepositoResponseDTO> obtenerDeposito(@PathVariable UUID id) {
        DepositoResponseDTO response = depositoService.obtenerDeposito(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar depósitos",
               description = "Lista todos los depósitos, opcionalmente solo los activos")
    public ResponseEntity<List<DepositoResponseDTO>> listarDepositos(
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        List<DepositoResponseDTO> response = depositoService.listarDepositos(soloActivos);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar depósito")
    public ResponseEntity<DepositoResponseDTO> actualizarDeposito(
            @PathVariable UUID id,
            @Valid @RequestBody DepositoUpdateRequestDTO request) {
        DepositoResponseDTO response = depositoService.actualizarDeposito(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/contenedores")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar contenedores en un depósito",
               description = "Obtiene todos los contenedores actualmente almacenados en un depósito")
    public ResponseEntity<List<ContenedorEnDepositoResponseDTO>> listarContenedoresEnDeposito(
            @PathVariable UUID id) {
        List<ContenedorEnDepositoResponseDTO> response = depositoService.listarContenedoresEnDeposito(id);
        return ResponseEntity.ok(response);
    }
}