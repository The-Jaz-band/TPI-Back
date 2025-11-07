package com.grupo81.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.grupo81.client.dto.googleMaps.*;

@FeignClient(
    name = "google-maps",
    url = "${microservices.google-maps.base-url}",
    configuration = FeignClientConfiguration.class
)
public interface GoogleMapsClient {
    
    @GetMapping("/directions/json")
    GoogleMapsDirectionsResponse obtenerDirecciones(
        @RequestParam("origin") String origin,
        @RequestParam("destination") String destination,
        @RequestParam("key") String apiKey
    );
}

// DTOs para Google Maps
/*
record GoogleMapsDirectionsResponse(
    String status,
    java.util.List<Route> routes
) {}

record Route(
    java.util.List<Leg> legs
) {}

record Leg(
    Distance distance,
    Duration duration
) {}

record Distance(
    Long value, // metros
    String text
) {}

record Duration(
    Long value, // segundos
    String text
) {}
*/