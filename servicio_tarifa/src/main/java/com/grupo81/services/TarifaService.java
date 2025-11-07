package com.grupo81.services;

import com.grupo81.dtos.request.*;
import com.grupo81.dtos.response.*;
import com.grupo81.entity.Tarifa;
import com.grupo81.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TarifaService {
    
    private final TarifaRepository tarifaRepository;
    
    // Códigos de tarifa predefinidos
    private static final String CODIGO_BASE_KM = "BASE_KM";
    private static final String CODIGO_COMBUSTIBLE = "COMBUSTIBLE_LITRO";
    private static final String CODIGO_CONSUMO_PROMEDIO = "CONSUMO_PROMEDIO_L_KM";
    private static final String CODIGO_GESTION = "GESTION_TRAMO";
    private static final String CODIGO_ESTADIA = "ESTADIA_DIARIO";
    
    @Transactional
    public TarifaResponseDTO crearTarifa(TarifaCreateRequestDTO request) {
        log.info("Creando tarifa: {}", request.getCodigoTarifa());
        
        if (tarifaRepository.existsByCodigoTarifa(request.getCodigoTarifa())) {
            throw new IllegalArgumentException("Ya existe una tarifa con el código: " + request.getCodigoTarifa());
        }
        
        Tarifa tarifa = Tarifa.builder()
            .codigoTarifa(request.getCodigoTarifa())
            .descripcion(request.getDescripcion())
            .valor(request.getValor())
            .unidad(Tarifa.UnidadMedida.valueOf(request.getUnidad()))
            .build();
        
        tarifa = tarifaRepository.save(tarifa);
        log.info("Tarifa creada con ID: {}", tarifa.getId());
        
        return mapToDTO(tarifa);
    }
    
    @Transactional(readOnly = true)
    public ConfiguracionTarifaDTO obtenerConfiguracion() {
        log.info("Obteniendo configuración de tarifas");
        
        return ConfiguracionTarifaDTO.builder()
            .costoBaseKm(obtenerValorTarifa(CODIGO_BASE_KM, new BigDecimal("5.0")))
            .valorLitroCombustible(obtenerValorTarifa(CODIGO_COMBUSTIBLE, new BigDecimal("1.5")))
            .consumoPromedioLKm(obtenerValorTarifa(CODIGO_CONSUMO_PROMEDIO, new BigDecimal("0.35")))
            .costoGestionPorTramo(obtenerValorTarifa(CODIGO_GESTION, new BigDecimal("100.0")))
            .costoEstadiaDiario(obtenerValorTarifa(CODIGO_ESTADIA, new BigDecimal("50.0")))
            .build();
    }
    
    @Transactional(readOnly = true)
    public CostoCalculadoDTO calcularCosto(CalculoCostoRequestDTO request) {
        log.info("Calculando costo para {} km, {} tramos", request.getDistanciaKm(), request.getCantidadTramos());
        
        ConfiguracionTarifaDTO config = obtenerConfiguracion();
        
        // Calcular costo de traslado
        BigDecimal costoTraslado = config.getCostoBaseKm()
            .multiply(request.getDistanciaKm())
            .setScale(2, RoundingMode.HALF_UP);
        
        // Calcular costo de combustible
        BigDecimal consumoLitros = config.getConsumoPromedioLKm()
            .multiply(request.getDistanciaKm())
            .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal costoCombustible = consumoLitros
            .multiply(config.getValorLitroCombustible())
            .setScale(2, RoundingMode.HALF_UP);
        
        // Calcular costo de estadía
        BigDecimal costoEstadia = BigDecimal.ZERO;
        if (request.getDiasEstadia() != null && request.getDiasEstadia().compareTo(BigDecimal.ZERO) > 0) {
            costoEstadia = config.getCostoEstadiaDiario()
                .multiply(request.getDiasEstadia())
                .setScale(2, RoundingMode.HALF_UP);
        }
        
        // Calcular costo de gestión
        BigDecimal costoGestion = config.getCostoGestionPorTramo()
            .multiply(new BigDecimal(request.getCantidadTramos()))
            .setScale(2, RoundingMode.HALF_UP);
        
        // Costo total
        BigDecimal costoTotal = costoTraslado
            .add(costoCombustible)
            .add(costoEstadia)
            .add(costoGestion);
        
        DetalleCalculoDTO detalle = DetalleCalculoDTO.builder()
            .distanciaKm(request.getDistanciaKm())
            .costoBaseKm(config.getCostoBaseKm())
            .valorLitroCombustible(config.getValorLitroCombustible())
            .consumoLitrosTotal(consumoLitros)
            .cantidadTramos(request.getCantidadTramos())
            .diasEstadia(request.getDiasEstadia())
            .build();
        
        return CostoCalculadoDTO.builder()
            .costoTotal(costoTotal)
            .costoTraslado(costoTraslado)
            .costoCombustible(costoCombustible)
            .costoEstadia(costoEstadia)
            .costoGestion(costoGestion)
            .detalle(detalle)
            .build();
    }
    
    @Transactional(readOnly = true)
    public List<TarifaResponseDTO> listarTarifas() {
        return tarifaRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public TarifaResponseDTO actualizarTarifa(UUID id, TarifaUpdateRequestDTO request) {
        Tarifa tarifa = tarifaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada"));
        
        if (request.getDescripcion() != null) {
            tarifa.setDescripcion(request.getDescripcion());
        }
        if (request.getValor() != null) {
            tarifa.setValor(request.getValor());
        }
        
        tarifa = tarifaRepository.save(tarifa);
        log.info("Tarifa actualizada: {}", tarifa.getCodigoTarifa());
        
        return mapToDTO(tarifa);
    }
    
    private BigDecimal obtenerValorTarifa(String codigo, BigDecimal valorDefault) {
        return tarifaRepository.findByCodigoTarifa(codigo)
            .map(Tarifa::getValor)
            .orElse(valorDefault);
    }
    
    private TarifaResponseDTO mapToDTO(Tarifa tarifa) {
        return TarifaResponseDTO.builder()
            .id(tarifa.getId())
            .codigoTarifa(tarifa.getCodigoTarifa())
            .descripcion(tarifa.getDescripcion())
            .valor(tarifa.getValor())
            .unidad(tarifa.getUnidad().name())
            .fechaCreacion(tarifa.getFechaCreacion())
            .fechaActualizacion(tarifa.getFechaActualizacion())
            .build();
    }
}