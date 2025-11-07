package com.grupo81.client.dto.googleMaps;

import java.util.List;

public record GoogleMapsDirectionsResponse(
    String status,
    List<Route> routes
) {}

