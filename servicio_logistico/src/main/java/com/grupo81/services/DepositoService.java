package com.grupo81.servicio_logistico.services;

import com.grupo81.servicio_logistico.dtos.deposito.request.*;
import com.grupo81.servicio_logistico.dtos.deposito.response.*;
import com.grupo81.servicio_logistico.dtos.deposito.response.ContenedorEnDepositoResponseDTO;
import com.grupo81.servicio_logistico.dtos.deposito.response.DepositoResponseDTO;
import com.grupo81.servicio_logistico.entity.*;
import com.grupo81.servicio_logistico.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositoService {
    
    private final DepositoRepository depositoRepository;
    private final TramoRepository tramoRepository;
    
    @Transactional
    public DepositoResponseDTO crearDeposito(DepositoCreateRequestDTO request) {
        log.info("Creando depósito: {}", request.getNombre());
        
        Deposito deposito = Deposito.builder()
            .nombre(request.getNombre())
            .direccion(request.getDireccion())
            .latitud(request.getLatitud())
            .longitud(request.getLongitud())
            .costoEstadiaDiario(request.getCostoEstadiaDiario())
            .activo(true)
            .build();
        
        deposito = depositoRepository.save(deposito);
        log.info("Depósito creado con ID: {}", deposito.getId());
        
        return mapToDTO(deposito);
    }
    
    @Transactional(readOnly = true)
    public DepositoResponseDTO obtenerDeposito(UUID id) {
        Deposito deposito = depositoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Depósito no encontrado con ID: " + id));
        return mapToDTO(deposito);
    }
    
    @Transactional(readOnly = true)
    public List<DepositoResponseDTO> listarDepositos(boolean soloActivos) {
        List<Deposito> depositos = soloActivos ? 
            depositoRepository.findByActivoTrue() : 
            depositoRepository.findAll();
        
        return depositos.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public DepositoResponseDTO actualizarDeposito(UUID id, DepositoUpdateRequestDTO request) {
        Deposito deposito = depositoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Depósito no encontrado"));
        
        if (request.getNombre() != null) {
            deposito.setNombre(request.getNombre());
        }
        if (request.getDireccion() != null) {
            deposito.setDireccion(request.getDireccion());
        }
        if (request.getLatitud() != null) {
            deposito.setLatitud(request.getLatitud());
        }
        if (request.getLongitud() != null) {
            deposito.setLongitud(request.getLongitud());
        }
        if (request.getCostoEstadiaDiario() != null) {
            deposito.setCostoEstadiaDiario(request.getCostoEstadiaDiario());
        }
        if (request.getActivo() != null) {
            deposito.setActivo(request.getActivo());
        }
        
        deposito = depositoRepository.save(deposito);
        log.info("Depósito actualizado: {}", deposito.getNombre());
        
        return mapToDTO(deposito);
    }
    
    @Transactional(readOnly = true)
    public List<ContenedorEnDepositoResponseDTO> listarContenedoresEnDeposito(UUID depositoId) {
        log.info("Consultando contenedores en depósito: {}", depositoId);
        
        Deposito deposito = depositoRepository.findById(depositoId)
            .orElseThrow(() -> new IllegalArgumentException("Depósito no encontrado"));
        
        List<Tramo> tramosEnDeposito = tramoRepository.findContenedoresEnDeposito(depositoId);
        
        return tramosEnDeposito.stream()
            .map(tramo -> {
                Ruta ruta = tramo.getRuta();
                Solicitud solicitud = ruta.getSolicitud();
                Contenedor contenedor = solicitud.getContenedor();
                
                // Buscar el próximo tramo
                List<Tramo> todosTramos = tramoRepository.findByRutaIdOrderByOrden(ruta.getId());
                Tramo proximoTramo = todosTramos.stream()
                    .filter(t -> t.getOrden() == tramo.getOrden() + 1)
                    .findFirst()
                    .orElse(null);
                
                return ContenedorEnDepositoResponseDTO.builder()
                    .contenedorId(contenedor.getId())
                    .identificacion(contenedor.getIdentificacion())
                    .solicitudId(solicitud.getId())
                    .numeroSolicitud(solicitud.getNumero())
                    .depositoId(deposito.getId())
                    .depositoNombre(deposito.getNombre())
                    .fechaIngreso(tramo.getFechaHoraFin())
                    .proximoTramoId(proximoTramo != null ? proximoTramo.getId() : null)
                    .proximoDestino(proximoTramo != null ? proximoTramo.getDestinoDireccion() : "Destino final")
                    .camionAsignado(proximoTramo != null && proximoTramo.getCamionId() != null)
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    private DepositoResponseDTO mapToDTO(Deposito deposito) {
        return DepositoResponseDTO.builder()
            .id(deposito.getId())
            .nombre(deposito.getNombre())
            .direccion(deposito.getDireccion())
            .latitud(deposito.getLatitud())
            .longitud(deposito.getLongitud())
            .costoEstadiaDiario(deposito.getCostoEstadiaDiario())
            .activo(deposito.getActivo())
            .fechaCreacion(deposito.getFechaCreacion())
            .fechaActualizacion(deposito.getFechaActualizacion())
            .build();
    }
}