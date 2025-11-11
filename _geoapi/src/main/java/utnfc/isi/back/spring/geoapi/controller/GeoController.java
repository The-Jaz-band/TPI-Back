package utnfc.isi.back.spring.geoapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utnfc.isi.back.spring.geoapi.model.DistanciaDTO;
import utnfc.isi.back.spring.geoapi.service.GeoService;

@RestController
@RequestMapping("/api/distancia")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Distancias", description = "Cálculo de distancias y tiempos usando Google Maps")
public class GeoController {
    
    private final GeoService geoService;
    
    @GetMapping
    @Operation(
        summary = "Calcular distancia entre dos puntos",
        description = "Usa Google Maps Distance Matrix API para calcular la distancia en kilómetros y tiempo estimado"
    )
    public ResponseEntity<DistanciaDTO> obtenerDistancia(
            @Parameter(description = "Coordenadas o dirección de origen (ej: '-31.4167,-64.1833' o 'Córdoba, Argentina')")
            @RequestParam String origen,
            @Parameter(description = "Coordenadas o dirección de destino")
            @RequestParam String destino) {
        
        log.info(" Calculando distancia: {} → {}", origen, destino);
        
        try {
            DistanciaDTO resultado = geoService.calcularDistancia(origen, destino);
            log.info(" Distancia calculada: {} km", resultado.getKilometros());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error(" Error calculando distancia: {}", e.getMessage(), e);
            throw new RuntimeException("Error al calcular distancia: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica que el servicio esté funcionando")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("GeoAPI Service is running ");
    }
}