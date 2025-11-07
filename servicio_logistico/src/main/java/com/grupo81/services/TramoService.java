package com.grupo81.servicio_logistico.services;

import com.grupo81.servicio_logistico.client.*;
import com.grupo81.servicio_logistico.client.dto.CamionDTO;
import com.grupo81.servicio_logistico.client.dto.ConfiguracionTarifaDTO;
import com.grupo81.servicio_logistico.dtos.ruta.request.*;
import com.grupo81.servicio_logistico.dtos.ruta.response.*;
import com.grupo81.servicio_logistico.entity.*;
import com.grupo81.servicio_logistico.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TramoService {
    
    private final TramoRepository tramoRepository;
    private final ContenedorRepository contenedorRepository;
    private final SolicitudRepository solicitudRepository;
    private final FlotaServiceClient flotaServiceClient;
    private final TarifaServiceClient tarifaServiceClient;
    
    @Transactional
    public TramoResponseDTO asignarCamion(UUID tramoId, AsignarCamionRequestDTO request) {
        log.info("Asignando camión {} al tramo {}", request.getCamionId(), tramoId);
        
        Tramo tramo = tramoRepository.findById(tramoId)
            .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));
        
        if (tramo.getEstado() != Tramo.EstadoTramo.ESTIMADO) {
            throw new IllegalStateException("El tramo ya tiene un camión asignado o está en progreso");
        }
        
        // Validar capacidad del camión
        Solicitud solicitud = tramo.getRuta().getSolicitud();
        Contenedor contenedor = solicitud.getContenedor();
        
        CamionDTO camion = flotaServiceClient.obtenerCamion(request.getCamionId());
        
        if (contenedor.getPesoKg().compareTo(camion.capacidadPesoKg()) > 0) {
            throw new IllegalArgumentException("El camión no tiene capacidad de peso suficiente");
        }
        
        if (contenedor.getVolumenM3().compareTo(camion.capacidadVolumenM3()) > 0) {
            throw new IllegalArgumentException("El camión no tiene capacidad de volumen suficiente");
        }
        
        // Asignar camión
        tramo.setCamionId(request.getCamionId());
        tramo.setEstado(Tramo.EstadoTramo.ASIGNADO);
        Tramo tramoActualizado = tramoRepository.save(tramo);
        
        // Actualizar disponibilidad del camión
        flotaServiceClient.actualizarDisponibilidad(request.getCamionId(), false);
        
        log.info("Camión asignado exitosamente al tramo {}", tramoId);
        
        return mapToDTO(tramoActualizado);
    }
    
    @Transactional
    public TramoResponseDTO iniciarTramo(UUID tramoId) {
        log.info("Iniciando tramo: {}", tramoId);
        
        Tramo tramo = tramoRepository.findById(tramoId)
            .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));
        
        if (tramo.getEstado() != Tramo.EstadoTramo.ASIGNADO) {
            throw new IllegalStateException("El tramo debe estar asignado para poder iniciarse");
        }
        
        if (tramo.getCamionId() == null) {
            throw new IllegalStateException("El tramo no tiene un camión asignado");
        }
        
        // Verificar que el tramo anterior esté finalizado (si existe)
        if (tramo.getOrden() > 1) {
            UUID rutaId = tramo.getRuta().getId();
            int ordenActual = tramo.getOrden();
            
            List<Tramo> tramosAnteriores = tramoRepository.findByRutaIdOrderByOrden(rutaId);
            Tramo tramoAnterior = tramosAnteriores.stream()
                .filter(t -> t.getOrden() == ordenActual - 1)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró el tramo anterior"));
            
            if (tramoAnterior.getEstado() != Tramo.EstadoTramo.FINALIZADO) {
                throw new IllegalStateException("El tramo anterior debe estar finalizado");
            }
        }
        
        tramo.setFechaHoraInicio(LocalDateTime.now());
        tramo.setEstado(Tramo.EstadoTramo.INICIADO);
        Tramo tramoIniciado = tramoRepository.save(tramo);
        
        // Actualizar estado del contenedor
        Contenedor contenedor = tramoIniciado.getRuta().getSolicitud().getContenedor();
        if (tramoIniciado.getOrden() == 1) {
            contenedor.setEstadoActual(Contenedor.EstadoContenedor.RETIRADO);
        }
        contenedor.setEstadoActual(Contenedor.EstadoContenedor.EN_VIAJE);
        contenedor.setUbicacionActualDireccion(tramoIniciado.getOrigenDireccion());
        contenedorRepository.save(contenedor);
        
        // Actualizar estado de la solicitud
        Solicitud solicitud = tramoIniciado.getRuta().getSolicitud();
        if (solicitud.getEstado() == Solicitud.EstadoSolicitud.PROGRAMADA) {
            solicitud.setEstado(Solicitud.EstadoSolicitud.EN_TRANSITO);
            solicitudRepository.save(solicitud);
        }
        
        log.info("Tramo iniciado exitosamente");
        
        return mapToDTO(tramoIniciado);
    }
    
    @Transactional
    public TramoResponseDTO finalizarTramo(UUID tramoId) {
        log.info("Finalizando tramo: {}", tramoId);
        
        Tramo tramo = tramoRepository.findById(tramoId)
            .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));
        
        if (tramo.getEstado() != Tramo.EstadoTramo.INICIADO) {
            throw new IllegalStateException("El tramo debe estar iniciado para poder finalizarse");
        }
        
        tramo.setFechaHoraFin(LocalDateTime.now());
        tramo.setEstado(Tramo.EstadoTramo.FINALIZADO);
        
        // Calcular costo real del tramo
        BigDecimal costoReal = calcularCostoRealTramo(tramo);
        tramo.setCostoReal(costoReal);
        
        Tramo tramoFinalizado = tramoRepository.save(tramo);
        
        // Actualizar contenedor
        Contenedor contenedor = tramoFinalizado.getRuta().getSolicitud().getContenedor();
        if (tramoFinalizado.getDeposito() != null) {
            contenedor.setEstadoActual(Contenedor.EstadoContenedor.EN_DEPOSITO);
            contenedor.setUbicacionActualDireccion(tramoFinalizado.getDeposito().getDireccion());
        } else {
            // Es el último tramo
            contenedor.setEstadoActual(Contenedor.EstadoContenedor.ENTREGADO);
            contenedor.setUbicacionActualDireccion(tramoFinalizado.getDestinoDireccion());
        }
        contenedorRepository.save(contenedor);
        
        // Liberar camión
        if (tramoFinalizado.getCamionId() != null) {
            flotaServiceClient.actualizarDisponibilidad(tramoFinalizado.getCamionId(), true);
        }
        
        // Verificar si es el último tramo para finalizar la solicitud
        verificarFinalizacionSolicitud(tramoFinalizado.getRuta());
        
        log.info("Tramo finalizado exitosamente");
        
        return mapToDTO(tramoFinalizado);
    }
    
    private BigDecimal calcularCostoRealTramo(Tramo tramo) {
        if (tramo.getCamionId() == null || tramo.getFechaHoraInicio() == null || tramo.getFechaHoraFin() == null) {
            return tramo.getCostoAproximado();
        }
        
        // Obtener datos del camión
        CamionDTO camion = flotaServiceClient.obtenerCamion(tramo.getCamionId());
        
        // Calcular costo de traslado
        BigDecimal costoTraslado = camion.costoBaseKm().multiply(tramo.getDistanciaKm());
        
        // Calcular costo de combustible
        BigDecimal costoCombustible = camion.consumoCombustibleLKm()
            .multiply(tramo.getDistanciaKm())
            .multiply(obtenerValorCombustible());
        
        // Calcular costo de estadía si aplica
        BigDecimal costoEstadia = BigDecimal.ZERO;
        if (tramo.getDeposito() != null) {
            long dias = Duration.between(tramo.getFechaHoraInicio(), tramo.getFechaHoraFin()).toDays();
            costoEstadia = tramo.getDeposito().getCostoEstadiaDiario().multiply(new BigDecimal(dias));
        }
        
        return costoTraslado.add(costoCombustible).add(costoEstadia);
    }
    
    private BigDecimal obtenerValorCombustible() {
        ConfiguracionTarifaDTO config = tarifaServiceClient.obtenerConfiguracion();
        return config.valorLitroCombustible();
    }
    
    private void verificarFinalizacionSolicitud(Ruta ruta) {
        List<Tramo> tramos = tramoRepository.findByRutaIdOrderByOrden(ruta.getId());
        
        boolean todosFinalizados = tramos.stream()
            .allMatch(t -> t.getEstado() == Tramo.EstadoTramo.FINALIZADO);
        
        if (todosFinalizados) {
            Solicitud solicitud = ruta.getSolicitud();
            solicitud.setEstado(Solicitud.EstadoSolicitud.ENTREGADA);
            solicitud.setFechaEntrega(LocalDateTime.now());
            
            // Calcular costos y tiempos reales
            BigDecimal costoReal = tramos.stream()
                .map(t -> t.getCostoReal() != null ? t.getCostoReal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            solicitud.setCostoFinal(costoReal);
            
            // Calcular tiempo real en horas
            if (!tramos.isEmpty()) {
                LocalDateTime inicio = tramos.get(0).getFechaHoraInicio();
                LocalDateTime fin = tramos.get(tramos.size() - 1).getFechaHoraFin();
                if (inicio != null && fin != null) {
                    long horas = Duration.between(inicio, fin).toHours();
                    solicitud.setTiempoRealHoras(new BigDecimal(horas));
                }
            }
            
            solicitudRepository.save(solicitud);
            log.info("Solicitud {} finalizada", solicitud.getNumero());
        }
    }
    
    @Transactional(readOnly = true)
    public List<TramoResponseDTO> obtenerTramosPorCamion(UUID camionId) {
        List<Tramo.EstadoTramo> estados = List.of(
            Tramo.EstadoTramo.ASIGNADO,
            Tramo.EstadoTramo.INICIADO
        );
        
        return tramoRepository.findByCamionIdAndEstadoIn(camionId, estados).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    private TramoResponseDTO mapToDTO(Tramo tramo) {
        return TramoResponseDTO.builder()
            .id(tramo.getId())
            .orden(tramo.getOrden())
            .tipo(tramo.getTipo().name())
            .estado(tramo.getEstado().name())
            .origenDireccion(tramo.getOrigenDireccion())
            .origenLatitud(tramo.getOrigenLatitud())
            .origenLongitud(tramo.getOrigenLongitud())
            .destinoDireccion(tramo.getDestinoDireccion())
            .destinoLatitud(tramo.getDestinoLatitud())
            .destinoLongitud(tramo.getDestinoLongitud())
            .distanciaKm(tramo.getDistanciaKm())
            .costoAproximado(tramo.getCostoAproximado())
            .costoReal(tramo.getCostoReal())
            .fechaHoraEstimadaInicio(tramo.getFechaHoraEstimadaInicio())
            .fechaHoraEstimadaFin(tramo.getFechaHoraEstimadaFin())
            .fechaHoraInicio(tramo.getFechaHoraInicio())
            .fechaHoraFin(tramo.getFechaHoraFin())
            .camionId(tramo.getCamionId())
            .depositoId(tramo.getDeposito() != null ? tramo.getDeposito().getId() : null)
            .build();
    }
}