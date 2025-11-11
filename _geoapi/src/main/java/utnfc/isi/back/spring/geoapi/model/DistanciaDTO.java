package utnfc.isi.back.spring.geoapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de distancia y tiempo entre dos puntos")
public class DistanciaDTO {
    
    @Schema(description = "Punto de origen", example = "Córdoba, Argentina")
    private String origen;
    
    @Schema(description = "Punto de destino", example = "Mendoza, Argentina")
    private String destino;
    
    @Schema(description = "Distancia en kilómetros", example = "461.5")
    private double kilometros;
    
    @Schema(description = "Tiempo estimado de viaje", example = "5 hours 30 mins")
    private String duracionTexto;
    
    @Schema(description = "Duración en segundos", example = "19800")
    private long duracionSegundos;
}