package com.grupo81.servicio_logistico.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.grupo81.servicio_logistico.client.dto.*;

import java.util.UUID;

// ============ Cliente Service Client ============

@FeignClient(
    name = "servicio-cliente",
    url = "${microservices.clientes.url}",
    configuration = FeignClientConfiguration.class
)
public interface ClienteServiceClient {
    
    @PostMapping("/api/clientes")
    ClienteDTO crearCliente(@RequestBody ClienteCreateDTO cliente);
    
    @GetMapping("/api/clientes/{id}")
    ClienteDTO obtenerCliente(@PathVariable("id") UUID id);
    
    @GetMapping("/api/clientes/email/{email}")
    ClienteDTO buscarPorEmail(@PathVariable("email") String email);
}

// DTOs para Cliente
/*public record ClienteCreateDTO(
    String nombre,
    String email,
    String telefono,
    String empresa
) {}

public record ClienteDTO(
    UUID id,
    String nombre,
    String email,
    String telefono,
    String empresa
) {}
*/