package com.grupo81.services;

import com.grupo81.client.GeoApiClient;
import com.grupo81.client.TarifaServiceClient;
import com.grupo81.client.dto.ConfiguracionTarifaDTO;
import com.grupo81.client.dto.geoapi.DistanciaDTO;
import com.grupo81.dtos.ruta.request.RutaAsignacionRequestDTO;
import com.grupo81.dtos.ruta.response.RutaResponseDTO;
import com.grupo81.dtos.ruta.response.RutaTentativaResponseDTO;
import com.grupo81.dtos.ruta.response.TramoResponseDTO;
import com.grupo81.dtos.ruta.response.TramoTentativoDTO;
import com.grupo81.entity.*;
import com.grupo81.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RutaService {
    
    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final SolicitudRepository solicitudRepository;
    private final DepositoRepository depositoRepository;
    private final GeoApiClient geoApiClient;  // ✅ Usar GeoAPI en vez de Google Maps directo
    private final TarifaServiceClient tarifaServiceClient;
    
    @Transactional(readOnly = true)
    public RutaTentativaResponseDTO calcularRutaTentativa(UUID solicitudId, List<UUID> depositosIds) {
        log.info("🚀 Calculando ruta tentativa para solicitud: {}", solicitudId);
        
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        
        List<Deposito> depositos = depositoRepository.findAllById(depositosIds);
        if (depositos.size() != depositosIds.size()) {
            throw new IllegalArgumentException("Algunos depósitos no fueron encontrados");
        }
        
        List<TramoTentativoDTO> tramos = new ArrayList<>();
        BigDecimal costoTotal = BigDecimal.ZERO;
        BigDecimal tiempoTotal = BigDecimal.ZERO;
        BigDecimal distanciaTotal = BigDecimal.ZERO;
        
        ConfiguracionTarifaDTO configuracion = tarifaServiceClient.obtenerConfiguracion();
        
        // Crear tramos
        String origenActual = formatearCoordenadas(solicitud.getOrigenLatitud(), solicitud.getOrigenLongitud());
        int orden = 1;
        
        // Primer tramo: origen a primer depósito
        if (!depositos.isEmpty()) {
            Deposito primerDeposito = depositos.get(0);
            String destinoDeposito = formatearCoordenadas(primerDeposito.getLatitud(), primerDeposito.getLongitud());
            
            TramoTentativoDTO tramo = calcularTramoTentativo(
                orden++,
                Tramo.TipoTramo.ORIGEN_DEPOSITO,
                solicitud.getOrigenDireccion(),
                primerDeposito.getDireccion(),
                origenActual,
                destinoDeposito,
                solicitud.getContenedor(),
                configuracion,
                primerDeposito
            );
            
            tramos.add(tramo);
            costoTotal = costoTotal.add(tramo.getCostoEstimado());
            tiempoTotal = tiempoTotal.add(tramo.getTiempoEstimadoHoras());
            distanciaTotal = distanciaTotal.add(tramo.getDistanciaKm());
            
            origenActual = destinoDeposito;
        }
        
        // Tramos entre depósitos
        for (int i = 0; i < depositos.size() - 1; i++) {
            Deposito origenDep = depositos.get(i);
            Deposito destinoDep = depositos.get(i + 1);
            
            String origen = formatearCoordenadas(origenDep.getLatitud(), origenDep.getLongitud());
            String destino = formatearCoordenadas(destinoDep.getLatitud(), destinoDep.getLongitud());
            
            TramoTentativoDTO tramo = calcularTramoTentativo(
                orden++,
                Tramo.TipoTramo.DEPOSITO_DEPOSITO,
                origenDep.getDireccion(),
                destinoDep.getDireccion(),
                origen,
                destino,
                solicitud.getContenedor(),
                configuracion,
                destinoDep
            );
            
            tramos.add(tramo);
            costoTotal = costoTotal.add(tramo.getCostoEstimado());
            tiempoTotal = tiempoTotal.add(tramo.getTiempoEstimadoHoras());
            distanciaTotal = distanciaTotal.add(tramo.getDistanciaKm());
            
            origenActual = destino;
        }
        
        // Último tramo: último depósito (o origen) a destino
        String destinoFinal = formatearCoordenadas(solicitud.getDestinoLatitud(), solicitud.getDestinoLongitud());
        Tramo.TipoTramo tipoUltimoTramo = depositos.isEmpty() ? 
            Tramo.TipoTramo.ORIGEN_DESTINO : Tramo.TipoTramo.DEPOSITO_DESTINO;
        
        TramoTentativoDTO ultimoTramo = calcularTramoTentativo(
            orden,
            tipoUltimoTramo,
            depositos.isEmpty() ? solicitud.getOrigenDireccion() : depositos.get(depositos.size() - 1).getDireccion(),
            solicitud.getDestinoDireccion(),
            origenActual,
            destinoFinal,
            solicitud.getContenedor(),
            configuracion,
            null
        );
        
        tramos.add(ultimoTramo);
        costoTotal = costoTotal.add(ultimoTramo.getCostoEstimado());
        tiempoTotal = tiempoTotal.add(ultimoTramo.getTiempoEstimadoHoras());
        distanciaTotal = distanciaTotal.add(ultimoTramo.getDistanciaKm());
        
        // Agregar costo de gestión
        BigDecimal costoGestion = configuracion.costoGestionPorTramo().multiply(new BigDecimal(tramos.size()));
        costoTotal = costoTotal.add(costoGestion);
        
        log.info("✅ Ruta tentativa calculada: {} tramos, {} km, ${}", tramos.size(), distanciaTotal, costoTotal);
        
        return RutaTentativaResponseDTO.builder()
            .solicitudId(solicitudId)
            .tramos(tramos)
            .costoEstimadoTotal(costoTotal)
            .tiempoEstimadoTotalHoras(tiempoTotal)
            .distanciaTotalKm(distanciaTotal)
            .build();
    }
    
    private TramoTentativoDTO calcularTramoTentativo(
            int orden,
            Tramo.TipoTramo tipo,
            String origenDireccion,
            String destinoDireccion,
            String origenCoord,
            String destinoCoord,
            Contenedor contenedor,
            ConfiguracionTarifaDTO configuracion,
            Deposito deposito) {
        
        log.info("📍 Calculando tramo {} ({} → {})", orden, origenDireccion, destinoDireccion);
        
        // ✅ Obtener distancia de GeoAPI
        BigDecimal distanciaKm = obtenerDistanciaGeoAPI(origenCoord, destinoCoord);
        
        // Calcular tiempo estimado en horas
        BigDecimal tiempoHoras = BigDecimal.valueOf(distanciaKm.doubleValue() / 60.0); // Asumiendo 60 km/h promedio
        
        // Calcular costos
        BigDecimal costoTraslado = configuracion.costoBaseKm().multiply(distanciaKm);
        BigDecimal costoCombustible = configuracion.consumoPromedioLKm()
            .multiply(distanciaKm)
            .multiply(configuracion.valorLitroCombustible());
        
        BigDecimal costoTotal = costoTraslado.add(costoCombustible);
        
        log.info("   💰 Distancia: {} km | Costo: ${}", distanciaKm, costoTotal);
        
        return TramoTentativoDTO.builder()
            .orden(orden)
            .tipo(tipo.name())
            .origenDireccion(origenDireccion)
            .destinoDireccion(destinoDireccion)
            .distanciaKm(distanciaKm)
            .costoEstimado(costoTotal)
            .tiempoEstimadoHoras(tiempoHoras)
            .depositoId(deposito != null ? deposito.getId() : null)
            .depositoNombre(deposito != null ? deposito.getNombre() : null)
            .build();
    }
    
    @Transactional
    public RutaResponseDTO asignarRuta(RutaAsignacionRequestDTO request) {
        log.info("🚀 Asignando ruta a solicitud: {}", request.getSolicitudId());
        
        Solicitud solicitud = solicitudRepository.findById(request.getSolicitudId())
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        
        if (solicitud.getEstado() != Solicitud.EstadoSolicitud.BORRADOR) {
            throw new IllegalStateException("La solicitud ya tiene una ruta asignada");
        }
        
        // Calcular ruta tentativa
        RutaTentativaResponseDTO rutaTentativa = calcularRutaTentativa(
            request.getSolicitudId(), 
            request.getDepositosIds()
        );
        
        // Crear ruta definitiva
        Ruta ruta = Ruta.builder()
            .solicitud(solicitud)
            .cantidadTramos(rutaTentativa.getTramos().size())
            .cantidadDepositos(request.getDepositosIds().size())
            .build();
        
        ruta = rutaRepository.save(ruta);
        log.info("✅ Ruta creada con ID: {}", ruta.getId());
        
        // Crear tramos
        LocalDateTime fechaEstimadaInicio = LocalDateTime.now().plusHours(1); // Inicia en 1 hora
        
        for (TramoTentativoDTO tramoTentativo : rutaTentativa.getTramos()) {
            Tramo tramo = crearTramoDesdeDTO(tramoTentativo, ruta, solicitud, fechaEstimadaInicio);
            ruta.addTramo(tramo);
            tramoRepository.save(tramo);
            
            // Siguiente tramo inicia cuando termina el anterior
            fechaEstimadaInicio = tramo.getFechaHoraEstimadaFin();
        }
        
        // Actualizar solicitud
        solicitud.setEstado(Solicitud.EstadoSolicitud.PROGRAMADA);
        solicitud.setCostoEstimado(rutaTentativa.getCostoEstimadoTotal());
        solicitud.setTiempoEstimadoHoras(rutaTentativa.getTiempoEstimadoTotalHoras());
        solicitudRepository.save(solicitud);
        
        log.info("✅ Ruta asignada exitosamente con {} tramos", ruta.getCantidadTramos());
        
        return mapToResponseDTO(ruta);
    }
    
    private Tramo crearTramoDesdeDTO(TramoTentativoDTO dto, Ruta ruta, Solicitud solicitud, LocalDateTime fechaInicio) {
        // ✅ Extraer coordenadas correctamente
        BigDecimal[] origenCoords = extraerCoordenadas(dto.getOrigenDireccion(), solicitud, dto.getDepositoId(), true);
        BigDecimal[] destinoCoords = extraerCoordenadas(dto.getDestinoDireccion(), solicitud, dto.getDepositoId(), false);
        
        // ✅ Calcular fechas estimadas
        long horasEstimadas = dto.getTiempoEstimadoHoras().longValue();
        long minutosEstimados = dto.getTiempoEstimadoHoras()
            .subtract(new BigDecimal(horasEstimadas))
            .multiply(new BigDecimal("60")).longValue();
        
        LocalDateTime estimadoFin = fechaInicio.plusHours(horasEstimadas).plusMinutes(minutosEstimados);
        
        return Tramo.builder()
            .ruta(ruta)
            .orden(dto.getOrden())
            .tipo(Tramo.TipoTramo.valueOf(dto.getTipo()))
            .estado(Tramo.EstadoTramo.ESTIMADO)
            .origenDireccion(dto.getOrigenDireccion())
            .origenLatitud(origenCoords[0])
            .origenLongitud(origenCoords[1])
            .destinoDireccion(dto.getDestinoDireccion())
            .destinoLatitud(destinoCoords[0])
            .destinoLongitud(destinoCoords[1])
            .distanciaKm(dto.getDistanciaKm())
            .costoAproximado(dto.getCostoEstimado())
            .fechaHoraEstimadaInicio(fechaInicio)       // ✅ Agregado
            .fechaHoraEstimadaFin(estimadoFin)          // ✅ Agregado
            .deposito(dto.getDepositoId() != null ? 
                depositoRepository.findById(dto.getDepositoId()).orElse(null) : null)
            .build();
    }
    
    /**
     * ✅ Método corregido para extraer coordenadas
     */
    private BigDecimal[] extraerCoordenadas(String direccion, Solicitud solicitud, UUID depositoId, boolean esOrigen) {
        // Si es el origen de la solicitud
        if (direccion.equals(solicitud.getOrigenDireccion())) {
            return new BigDecimal[]{solicitud.getOrigenLatitud(), solicitud.getOrigenLongitud()};
        }
        
        // Si es el destino de la solicitud
        if (direccion.equals(solicitud.getDestinoDireccion())) {
            return new BigDecimal[]{solicitud.getDestinoLatitud(), solicitud.getDestinoLongitud()};
        }
        
        // Si es un depósito
        if (depositoId != null) {
            Deposito deposito = depositoRepository.findById(depositoId)
                .orElseThrow(() -> new IllegalArgumentException("Depósito no encontrado: " + depositoId));
            return new BigDecimal[]{deposito.getLatitud(), deposito.getLongitud()};
        }
        
        throw new IllegalStateException("No se pudieron determinar las coordenadas para: " + direccion);
    }
    
    /**
     * ✅ Método para obtener distancia usando GeoAPI
     */
    private BigDecimal obtenerDistanciaGeoAPI(String origen, String destino) {
        log.info("🗺️  Consultando GeoAPI: {} → {}", origen, destino);
        
        try {
            DistanciaDTO distancia = geoApiClient.calcularDistancia(origen, destino);
            BigDecimal distanciaKm = BigDecimal.valueOf(distancia.getKilometros())
                .setScale(2, RoundingMode.HALF_UP);
            
            log.info("✅ Distancia real obtenida de GeoAPI: {} km", distanciaKm);
            return distanciaKm;
            
        } catch (Exception e) {
            log.error("❌ Error al consultar GeoAPI: {}", e.getMessage(), e);
            log.warn("⚠️  Usando distancia estimada de 100 km (fallback)");
            return new BigDecimal("100");
        }
    }
    
    private String formatearCoordenadas(BigDecimal latitud, BigDecimal longitud) {
        return latitud.toString() + "," + longitud.toString();
    }
    
    private RutaResponseDTO mapToResponseDTO(Ruta ruta) {
        return RutaResponseDTO.builder()
            .id(ruta.getId())
            .solicitudId(ruta.getSolicitud().getId())
            .cantidadTramos(ruta.getCantidadTramos())
            .cantidadDepositos(ruta.getCantidadDepositos())
            .tramos(ruta.getTramos().stream()
                .map(this::mapTramoToDTO)
                .collect(Collectors.toList()))
            .fechaCreacion(ruta.getFechaCreacion())
            .build();
    }
    
    private TramoResponseDTO mapTramoToDTO(Tramo tramo) {
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