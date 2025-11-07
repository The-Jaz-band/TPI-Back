package com.grupo81.servicio_flota.services;

import com.grupo81.servicio_flota.dtos.request.*;
import com.grupo81.servicio_flota.dtos.response.*;
import com.grupo81.servicio_flota.entity.Camion;
import com.grupo81.servicio_flota.repository.CamionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CamionService {
    
    private final CamionRepository camionRepository;
    
    @Transactional
    public CamionResponseDTO crearCamion(CamionCreateRequestDTO request) {
        log.info("Creando camión con dominio: {}", request.getDominio());
        
        if (camionRepository.existsByDominio(request.getDominio())) {
            throw new IllegalArgumentException("Ya existe un camión con el dominio: " + request.getDominio());
        }
        
        Camion camion = Camion.builder()
            .dominio(request.getDominio())
            .nombreTransportista(request.getNombreTransportista())
            .telefonoTransportista(request.getTelefonoTransportista())
            .capacidadPesoKg(request.getCapacidadPesoKg())
            .capacidadVolumenM3(request.getCapacidadVolumenM3())
            .costoBaseKm(request.getCostoBaseKm())
            .consumoCombustibleLKm(request.getConsumoCombustibleLKm())
            .disponible(true)
            .build();
        
        camion = camionRepository.save(camion);
        log.info("Camión creado con ID: {}", camion.getId());
        
        return mapToDTO(camion);
    }
    
    @Transactional(readOnly = true)
    public CamionResponseDTO obtenerCamion(UUID id) {
        Camion camion = camionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Camión no encontrado con ID: " + id));
        return mapToDTO(camion);
    }
    
    @Transactional(readOnly = true)
    public List<CamionResponseDTO> obtenerCamionesDisponibles(BigDecimal pesoMinimo, BigDecimal volumenMinimo) {
        log.info("Buscando camiones disponibles para peso: {} y volumen: {}", pesoMinimo, volumenMinimo);
        
        return camionRepository.findCamionesDisponibles(pesoMinimo, volumenMinimo).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CamionResponseDTO> listarCamiones() {
        return camionRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CamionResponseDTO> listarPorDisponibilidad(boolean disponible) {
        return camionRepository.findByDisponible(disponible).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public CamionResponseDTO actualizarCamion(UUID id, CamionUpdateRequestDTO request) {
        Camion camion = camionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Camión no encontrado"));
        
        if (request.getNombreTransportista() != null) {
            camion.setNombreTransportista(request.getNombreTransportista());
        }
        if (request.getTelefonoTransportista() != null) {
            camion.setTelefonoTransportista(request.getTelefonoTransportista());
        }
        if (request.getCostoBaseKm() != null) {
            camion.setCostoBaseKm(request.getCostoBaseKm());
        }
        if (request.getConsumoCombustibleLKm() != null) {
            camion.setConsumoCombustibleLKm(request.getConsumoCombustibleLKm());
        }
        if (request.getDisponible() != null) {
            camion.setDisponible(request.getDisponible());
        }
        
        camion = camionRepository.save(camion);
        return mapToDTO(camion);
    }
    
    @Transactional
    public void actualizarDisponibilidad(UUID id, boolean disponible) {
        Camion camion = camionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Camión no encontrado"));
        
        camion.setDisponible(disponible);
        camionRepository.save(camion);
        log.info("Disponibilidad del camión {} actualizada a: {}", id, disponible);
    }
    
    private CamionResponseDTO mapToDTO(Camion camion) {
        return CamionResponseDTO.builder()
            .id(camion.getId())
            .dominio(camion.getDominio())
            .nombreTransportista(camion.getNombreTransportista())
            .telefonoTransportista(camion.getTelefonoTransportista())
            .capacidadPesoKg(camion.getCapacidadPesoKg())
            .capacidadVolumenM3(camion.getCapacidadVolumenM3())
            .costoBaseKm(camion.getCostoBaseKm())
            .consumoCombustibleLKm(camion.getConsumoCombustibleLKm())
            .disponible(camion.getDisponible())
            .fechaCreacion(camion.getFechaCreacion())
            .build();
    }
}