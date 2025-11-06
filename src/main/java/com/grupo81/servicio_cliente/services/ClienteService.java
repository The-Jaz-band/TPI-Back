package com.grupo81.servicio_cliente.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo81.servicio_cliente.dtos.cliente.request.ClienteCreateRequestDTO;
import com.grupo81.servicio_cliente.dtos.cliente.request.ClienteUpdateRequestDTO;
import com.grupo81.servicio_cliente.dtos.cliente.response.ClienteResponseDTO;
import com.grupo81.servicio_cliente.entity.Cliente;
import com.grupo81.servicio_cliente.repository.ClienteRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {
    
    private final ClienteRepository clienteRepository;
    
    @Transactional
    public ClienteResponseDTO crearCliente(ClienteCreateRequestDTO request) {
        log.info("Creando cliente con email: {}", request.getEmail());
        
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con el email: " + request.getEmail());
        }
        
        Cliente cliente = Cliente.builder()
            .nombre(request.getNombre())
            .email(request.getEmail())
            .telefono(request.getTelefono())
            .empresa(request.getEmpresa())
            .build();
        
        cliente = clienteRepository.save(cliente);
        log.info("Cliente creado con ID: {}", cliente.getId());
        
        return mapToDTO(cliente);
    }
    
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerCliente(UUID id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        return mapToDTO(cliente);
    }
    
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorEmail(String email) {
        Cliente cliente = clienteRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con email: " + email));
        return mapToDTO(cliente);
    }
    
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarClientes() {
        return clienteRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public ClienteResponseDTO actualizarCliente(UUID id, ClienteUpdateRequestDTO request) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        
        if (request.getNombre() != null) {
            cliente.setNombre(request.getNombre());
        }
        if (request.getTelefono() != null) {
            cliente.setTelefono(request.getTelefono());
        }
        if (request.getEmpresa() != null) {
            cliente.setEmpresa(request.getEmpresa());
        }
        
        cliente = clienteRepository.save(cliente);
        return mapToDTO(cliente);
    }
    
    private ClienteResponseDTO mapToDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
            .id(cliente.getId())
            .nombre(cliente.getNombre())
            .email(cliente.getEmail())
            .telefono(cliente.getTelefono())
            .empresa(cliente.getEmpresa())
            .fechaCreacion(cliente.getFechaCreacion())
            .build();
    }
}