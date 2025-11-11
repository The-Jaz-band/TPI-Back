package com.grupo81.client.dto.openrouteservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrsSummary(
    double distance, // en metros
    double duration  // en segundos
) {}