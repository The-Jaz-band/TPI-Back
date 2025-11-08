package utnfc.isi.back.spring.geoapi.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import utnfc.isi.back.spring.geoapi.model.DistanciaDTO;
import utnfc.isi.back.spring.geoapi.service.GeoService;

@RestController
@RequestMapping("/api/distancia")
@RequiredArgsConstructor
public class GeoController {
 private final GeoService geoService;
 @GetMapping
 public DistanciaDTO obtenerDistancia(
 @RequestParam String origen,
 @RequestParam String destino) throws Exception {
 return geoService.calcularDistancia(origen, destino);
 }
}
