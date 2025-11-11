package com.grupo81.services;

import com.grupo81.client.*;
import com.grupo81.client.dto.ConfiguracionTarifaDTO;
import com.grupo81.client.dto.openrouteservice.OrsDirectionsResponse;
import com.grupo81.client.dto.openrouteservice.OrsSummary;
import com.grupo81.dtos.ruta.request.RutaAsignacionRequestDTO;
import com.grupo81.dtos.ruta.response.RutaResponseDTO;
import com.grupo81.dtos.ruta.response.RutaTentativaResponseDTO;
import com.grupo81.dtos.ruta.response.TramoResponseDTO;
import com.grupo81.dtos.ruta.response.TramoTentativoDTO;
import com.grupo81.entity.*;
import com.grupo81.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration; // <-- Importante
import java.time.LocalDateTime; // <-- Importante
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
    
    private final OpenRouteServiceClient openRouteServiceClient; 
    private final TarifaServiceClient tarifaServiceClient;
    
    @Value("${microservices.openrouteservice.api-key}")
    private String orsApiKey;
    
    private record InfoRuta(BigDecimal distanciaKm, BigDecimal duracionHoras) {}
    
    @Transactional(readOnly = true)
    public RutaTentativaResponseDTO calcularRutaTentativa(UUID solicitudId, List<UUID> depositosIds) {
        log.info("Calculando ruta tentativa para solicitud: {}", solicitudId);
        
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
        
        String origenActual = formatearCoordenadas(solicitud.getOrigenLatitud(), solicitud.getOrigenLongitud());
        int orden = 1;
        
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
        
        BigDecimal costoGestion = configuracion.costoGestionPorTramo().multiply(new BigDecimal(tramos.size()));
        costoTotal = costoTotal.add(costoGestion);
        
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
        
        InfoRuta infoRuta = obtenerInfoRuta(origenCoord, destinoCoord);
        
        BigDecimal distanciaKm = infoRuta.distanciaKm();
        BigDecimal tiempoHoras = infoRuta.duracionHoras();
        
        BigDecimal costoTraslado = configuracion.costoBaseKm().multiply(distanciaKm);
        BigDecimal costoCombustible = configuracion.consumoPromedioLKm()
            .multiply(distanciaKm)
            .multiply(configuracion.valorLitroCombustible());
        
        BigDecimal costoTotal = costoTraslado.add(costoCombustible);
        
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
    
    // --- MÉTODO ACTUALIZADO ---
    @Transactional
    public RutaResponseDTO asignarRuta(RutaAsignacionRequestDTO request) {
        log.info("Asignando ruta a solicitud: {}", request.getSolicitudId());
        
        Solicitud solicitud = solicitudRepository.findById(request.getSolicitudId())
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        
        if (solicitud.getEstado() != Solicitud.EstadoSolicitud.BORRADOR) {
            throw new IllegalStateException("La solicitud ya tiene una ruta asignada");
        }
        
        // 1. Calcular ruta tentativa (como antes)
        RutaTentativaResponseDTO rutaTentativa = calcularRutaTentativa(
            request.getSolicitudId(), 
            request.getDepositosIds()
        );
        
        // 2. Crear ruta definitiva
        Ruta ruta = Ruta.builder()
            .solicitud(solicitud)
            .cantidadTramos(rutaTentativa.getTramos().size())
            .cantidadDepositos(request.getDepositosIds().size())
            .build();
        
        ruta = rutaRepository.save(ruta);
        
        // --- INICIO DE LA LÓGICA DE FECHAS ---
        // 3. Inicializar la fecha de inicio estimada para el primer tramo
        LocalDateTime proximaFechaEstimadaInicio = LocalDateTime.now();
        
        for (TramoTentativoDTO dto : rutaTentativa.getTramos()) {
            
            // 4. Calcular la duración estimada en minutos
            BigDecimal tiempoHoras = dto.getTiempoEstimadoHoras();
            long totalMinutos = tiempoHoras.multiply(new BigDecimal("60")).longValue();
            Duration duracionEstimada = Duration.ofMinutes(totalMinutos);
            
            // 5. Calcular las fechas estimadas para este tramo
            LocalDateTime fechaEstimadaInicio = proximaFechaEstimadaInicio;
            LocalDateTime fechaEstimadaFin = fechaEstimadaInicio.plus(duracionEstimada);
            
            // 6. Extraer coordenadas (lógica de 'crearTramoDesdeDTO' de antes)
            BigDecimal[] origenCoords = extraerCoordenadas(dto.getOrigenDireccion(), solicitud, true, dto.getOrden() - 1);
            BigDecimal[] destinoCoords = extraerCoordenadas(dto.getDestinoDireccion(), solicitud, false, dto.getOrden() - 1);
            
            // 7. Construir el Tramo con las fechas estimadas
            Tramo tramo = Tramo.builder()
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
                .deposito(dto.getDepositoId() != null ? 
                    depositoRepository.findById(dto.getDepositoId()).orElse(null) : null)
                // --- CAMPOS NUEVOS ASIGNADOS ---
                .fechaHoraEstimadaInicio(fechaEstimadaInicio)
                .fechaHoraEstimadaFin(fechaEstimadaFin)
                .build();
            
            ruta.addTramo(tramo);
            tramoRepository.save(tramo);
            
            // 8. La fecha de inicio del próximo tramo es el fin de este
            proximaFechaEstimadaInicio = fechaEstimadaFin; 
        }
        
        // 9. Actualizar solicitud
        solicitud.setEstado(Solicitud.EstadoSolicitud.PROGRAMADA);
        solicitud.setCostoEstimado(rutaTentativa.getCostoEstimadoTotal());
        solicitud.setTiempoEstimadoHoras(rutaTentativa.getTiempoEstimadoTotalHoras());
        solicitudRepository.save(solicitud);
        
        log.info("Ruta asignada exitosamente con ID: {}", ruta.getId());
        
        return mapToResponseDTO(ruta);
    }
    
    // --- MÉTODO ELIMINADO ---
    // private Tramo crearTramoDesdeDTO(...) { ... }
    
    
    private BigDecimal[] extraerCoordenadas(String direccion, Solicitud solicitud, boolean esOrigen, int orden) {
        if (esOrigen && orden == 0) {
            return new BigDecimal[]{solicitud.getOrigenLatitud(), solicitud.getOrigenLongitud()};
        }
        
        if (!esOrigen && direccion.equals(solicitud.getDestinoDireccion())) {
            return new BigDecimal[]{solicitud.getDestinoLatitud(), solicitud.getDestinoLongitud()};
        }
        
        Deposito dep = depositoRepository.findByDireccionAndActivoTrue(direccion).orElse(null);
        if (dep != null) {
            return new BigDecimal[]{dep.getLatitud(), dep.getLongitud()};
        }

        log.warn("No se pudieron extraer coordenadas para la dirección: {}. Usando (0,0)", direccion);
        return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
    }
    
    private InfoRuta obtenerInfoRuta(String origen, String destino) {
        try {
            log.debug("Llamando a ORS: {} -> {}", origen, destino);
            OrsDirectionsResponse response = openRouteServiceClient.obtenerDirecciones(
                orsApiKey, origen, destino
            );
            
            if (response != null && response.features() != null && !response.features().isEmpty()) {
                OrsSummary summary = response.features().get(0).properties().summary();
                
                BigDecimal distanciaKm = new BigDecimal(summary.distance())
                    .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
                
                BigDecimal duracionHoras = new BigDecimal(summary.duration())
                    .divide(new BigDecimal("3600"), 2, RoundingMode.HALF_UP);
                
                log.info("Ruta calculada por ORS: {} km, {} horas", distanciaKm, duracionHoras);
                return new InfoRuta(distanciaKm, duracionHoras);
            }
        } catch (Exception e) {
            log.error("Error al obtener ruta de OpenRouteService: {}", e.getMessage(), e);
        }
        
        log.warn("Usando valores de fallback para ruta: 100km, 1.67h");
        BigDecimal fallbackDistancia = new BigDecimal("100.00");
        BigDecimal fallbackHoras = new BigDecimal("1.67");
        return new InfoRuta(fallbackDistancia, fallbackHoras);
    }
    
    private String formatearCoordenadas(BigDecimal latitud, BigDecimal longitud) {
        return longitud.toString() + "," + latitud.toString();
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
            .fechaHoraEstimadaInicio(tramo.getFechaHoraEstimadaInicio()) // <-- Ahora tendrán valor
            .fechaHoraEstimadaFin(tramo.getFechaHoraEstimadaFin())       // <-- Ahora tendrán valor
            .fechaHoraInicio(tramo.getFechaHoraInicio())
            .fechaHoraFin(tramo.getFechaHoraFin())
            .camionId(tramo.getCamionId())
            .depositoId(tramo.getDeposito() != null ? tramo.getDeposito().getId() : null)
            .build();
    }
}