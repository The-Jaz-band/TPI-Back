package com.grupo81.servicio_logistico.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo81.servicio_logistico.client.ClienteCreateDTO;
import com.grupo81.servicio_logistico.client.ClienteDTO;
import com.grupo81.servicio_logistico.client.ClienteServiceClient;
import com.grupo81.servicio_logistico.dtos.solicitud.request.SolicitudCreateRequestDTO;
import com.grupo81.servicio_logistico.dtos.solicitud.request.UbicacionDTO;
import com.grupo81.servicio_logistico.dtos.solicitud.request.ClienteRequestDTO;
import com.grupo81.servicio_logistico.dtos.solicitud.response.ContenedorResponseDTO;
import com.grupo81.servicio_logistico.dtos.solicitud.response.ContenedorSeguimientoDTO;
import com.grupo81.servicio_logistico.dtos.solicitud.response.SolicitudResponseDTO;
import com.grupo81.servicio_logistico.dtos.solicitud.response.SolicitudSeguimientoResponseDTO;
import com.grupo81.servicio_logistico.entity.Contenedor;
import com.grupo81.servicio_logistico.entity.Solicitud;
import com.grupo81.servicio_logistico.repository.ContenedorRepository;
import com.grupo81.servicio_logistico.repository.SolicitudRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudService {
    
    private final SolicitudRepository solicitudRepository;
    private final ContenedorRepository contenedorRepository;
    private final ClienteServiceClient clienteServiceClient;
    
    @Transactional
    public SolicitudResponseDTO crearSolicitud(SolicitudCreateRequestDTO request) {
        log.info("Iniciando creación de solicitud");
        
        // Validar que el contenedor no exista
        if (contenedorRepository.existsByIdentificacion(request.getContenedor().getIdentificacion())) {
            throw new IllegalArgumentException("Ya existe un contenedor con la identificación: " + 
                request.getContenedor().getIdentificacion());
        }
        
        // Crear o buscar cliente
        UUID clienteId = obtenerOCrearCliente(request.getCliente());
        log.info("Cliente procesado con ID: {}", clienteId);
        
        // Crear contenedor
        Contenedor contenedor = Contenedor.builder()
            .identificacion(request.getContenedor().getIdentificacion())
            .pesoKg(request.getContenedor().getPesoKg())
            .volumenM3(request.getContenedor().getVolumenM3())
            .estadoActual(Contenedor.EstadoContenedor.EN_ORIGEN)
            .ubicacionActualDireccion(request.getOrigen().getDireccion())
            .clienteId(clienteId)
            .build();
        
        contenedor = contenedorRepository.save(contenedor);
        log.info("Contenedor creado con ID: {}", contenedor.getId());
        
        // Crear solicitud
        Solicitud solicitud = Solicitud.builder()
            .numero(generarNumeroSolicitud())
            .contenedor(contenedor)
            .clienteId(clienteId)
            .origenDireccion(request.getOrigen().getDireccion())
            .origenLatitud(request.getOrigen().getLatitud())
            .origenLongitud(request.getOrigen().getLongitud())
            .destinoDireccion(request.getDestino().getDireccion())
            .destinoLatitud(request.getDestino().getLatitud())
            .destinoLongitud(request.getDestino().getLongitud())
            .estado(Solicitud.EstadoSolicitud.BORRADOR)
            .build();
        
        solicitud = solicitudRepository.save(solicitud);
        log.info("Solicitud creada con número: {}", solicitud.getNumero());
        
        return mapToResponseDTO(solicitud);
    }
    
    private UUID obtenerOCrearCliente(ClienteRequestDTO clienteRequest) {
        try {
            // Intentar buscar cliente existente por email
            ClienteDTO clienteExistente = clienteServiceClient.buscarPorEmail(clienteRequest.getEmail());
            return clienteExistente.id();
        } catch (Exception e) {
            // Si no existe, crear nuevo cliente
            ClienteCreateDTO nuevoCliente = new ClienteCreateDTO(
                clienteRequest.getNombre(),
                clienteRequest.getEmail(),
                clienteRequest.getTelefono(),
                clienteRequest.getEmpresa()
            );
            ClienteDTO clienteCreado = clienteServiceClient.crearCliente(nuevoCliente);
            return clienteCreado.id();
        }
    }
    
    private String generarNumeroSolicitud() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long count = solicitudRepository.countSolicitudesToday() + 1;
        return String.format("SOL-%s-%04d", fecha, count);
    }
    
    @Transactional(readOnly = true)
    public SolicitudResponseDTO obtenerSolicitud(UUID id) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada con ID: " + id));
        return mapToResponseDTO(solicitud);
    }
    
    @Transactional(readOnly = true)
    public SolicitudSeguimientoResponseDTO obtenerSeguimiento(UUID solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        
        // Aquí se puede inyectar TramoRepository para obtener el tramo actual
        // Por simplicidad, retornamos datos básicos
        
        return SolicitudSeguimientoResponseDTO.builder()
            .solicitudId(solicitud.getId())
            .numero(solicitud.getNumero())
            .estadoSolicitud(solicitud.getEstado().name())
            .contenedor(ContenedorSeguimientoDTO.builder()
                .identificacion(solicitud.getContenedor().getIdentificacion())
                .estadoActual(solicitud.getContenedor().getEstadoActual().name())
                .ubicacionActual(solicitud.getContenedor().getUbicacionActualDireccion())
                .build())
            .costoEstimado(solicitud.getCostoEstimado())
            .tiempoEstimadoHoras(solicitud.getTiempoEstimadoHoras())
            .fechaCreacion(solicitud.getFechaCreacion())
            .build();
    }
    
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarSolicitudesPorCliente(UUID clienteId) {
        return solicitudRepository.findByClienteId(clienteId).stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> listarSolicitudesPendientes() {
        List<Solicitud.EstadoSolicitud> estadosPendientes = List.of(
            Solicitud.EstadoSolicitud.BORRADOR,
            Solicitud.EstadoSolicitud.PROGRAMADA,
            Solicitud.EstadoSolicitud.EN_TRANSITO
        );
        
        return solicitudRepository.findByEstadoIn(estadosPendientes).stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }
    
    private SolicitudResponseDTO mapToResponseDTO(Solicitud solicitud) {
        return SolicitudResponseDTO.builder()
            .id(solicitud.getId())
            .numero(solicitud.getNumero())
            .contenedor(mapContenedorToDTO(solicitud.getContenedor()))
            .clienteId(solicitud.getClienteId())
            .origen(UbicacionDTO.builder()
                .direccion(solicitud.getOrigenDireccion())
                .latitud(solicitud.getOrigenLatitud())
                .longitud(solicitud.getOrigenLongitud())
                .build())
            .destino(UbicacionDTO.builder()
                .direccion(solicitud.getDestinoDireccion())
                .latitud(solicitud.getDestinoLatitud())
                .longitud(solicitud.getDestinoLongitud())
                .build())
            .estado(solicitud.getEstado().name())
            .costoEstimado(solicitud.getCostoEstimado())
            .tiempoEstimadoHoras(solicitud.getTiempoEstimadoHoras())
            .costoFinal(solicitud.getCostoFinal())
            .tiempoRealHoras(solicitud.getTiempoRealHoras())
            .fechaCreacion(solicitud.getFechaCreacion())
            .fechaEntrega(solicitud.getFechaEntrega())
            .build();
    }
    
    private ContenedorResponseDTO mapContenedorToDTO(Contenedor contenedor) {
        return ContenedorResponseDTO.builder()
            .id(contenedor.getId())
            .identificacion(contenedor.getIdentificacion())
            .pesoKg(contenedor.getPesoKg())
            .volumenM3(contenedor.getVolumenM3())
            .estadoActual(contenedor.getEstadoActual().name())
            .ubicacionActualDireccion(contenedor.getUbicacionActualDireccion())
            .build();
    }
}