package utnfc.isi.back.spring.geoapi.model;

Google_maps_distance_matrix.md 2025-10-21
5 / 7
import lombok.Data;
@Data
public class DistanciaDTO {
 private String origen;
 private String destino;
 private double kilometros;
 private String duracionTexto;
}