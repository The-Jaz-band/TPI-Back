package com.grupo81.servicio_cliente.controller;

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

import com.grupo81.servicio_cliente.dtos.cliente.request.ClienteCreateRequestDTO;
import com.grupo81.servicio_cliente.dtos.cliente.request.ClienteUpdateRequestDTO;
import com.grupo81.servicio_cliente.dtos.cliente.response.ClienteResponseDTO;
import com.grupo81.servicio_cliente.services.ClienteService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clientes", description = "Gestión de clientes")
@SecurityRequirement(name = "bearer-jwt")
public class ClienteController {
    
    private final ClienteService clienteService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Crear nuevo cliente")
    public ResponseEntity<ClienteResponseDTO> crearCliente(
            @Valid @RequestBody ClienteCreateRequestDTO request) {
        ClienteResponseDTO response = clienteService.crearCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<ClienteResponseDTO> obtenerCliente(@PathVariable UUID id) {
        ClienteResponseDTO response = clienteService.obtenerCliente(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Buscar cliente por email")
    public ResponseEntity<ClienteResponseDTO> buscarPorEmail(@PathVariable String email) {
        ClienteResponseDTO response = clienteService.buscarPorEmail(email);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar todos los clientes")
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        List<ClienteResponseDTO> response = clienteService.listarClientes();
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Actualizar cliente")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteUpdateRequestDTO request) {
        ClienteResponseDTO response = clienteService.actualizarCliente(id, request);
        return ResponseEntity.ok(response);
    }
}