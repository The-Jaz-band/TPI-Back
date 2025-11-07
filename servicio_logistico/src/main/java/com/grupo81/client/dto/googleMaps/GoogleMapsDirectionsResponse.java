package com.grupo81.servicio_logistico.client.dto.googleMaps;

import java.util.List;

public record GoogleMapsDirectionsResponse(
    String status,
    List<Route> routes
) {}

