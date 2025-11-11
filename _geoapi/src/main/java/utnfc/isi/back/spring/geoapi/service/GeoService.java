package utnfc.isi.back.spring.geoapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import utnfc.isi.back.spring.geoapi.model.DistanciaDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoService {

    @Value("${google.maps.apikey}")
    private String apiKey;

    private final RestClient.Builder builder;

    public DistanciaDTO calcularDistancia(String origen, String destino) throws Exception {
        log.info("🗺️  Llamando a Google Maps Distance Matrix API");
        
        RestClient client = builder
            .baseUrl("https://maps.googleapis.com/maps/api")
            .build();

        String url = String.format(
            "/distancematrix/json?origins=%s&destinations=%s&units=metric&key=%s",
            origen, destino, apiKey
        );

        log.debug("📡 URL: https://maps.googleapis.com/maps/api{}", url);

        ResponseEntity<String> response = client.get()
            .uri(url)
            .retrieve()
            .toEntity(String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error en la respuesta de Google Maps: " + response.getStatusCode());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        
        // Validar status de la respuesta
        String status = root.path("status").asText();
        if (!"OK".equals(status)) {
            log.error(" Google Maps status: {}", status);
            throw new RuntimeException("Google Maps API error: " + status);
        }

        JsonNode element = root.path("rows").get(0).path("elements").get(0);
        String elementStatus = element.path("status").asText();
        
        if (!"OK".equals(elementStatus)) {
            log.error(" Element status: {}", elementStatus);
            throw new RuntimeException("No se pudo calcular la ruta: " + elementStatus);
        }

        double distanciaMetros = element.path("distance").path("value").asDouble();
        double kilometros = distanciaMetros / 1000;
        String duracionTexto = element.path("duration").path("text").asText();
        long duracionSegundos = element.path("duration").path("value").asLong();

        log.info(" Distancia: {} km | Duración: {}", kilometros, duracionTexto);

        return DistanciaDTO.builder()
            .origen(origen)
            .destino(destino)
            .kilometros(kilometros)
            .duracionTexto(duracionTexto)
            .duracionSegundos(duracionSegundos)
            .build();
    }
}