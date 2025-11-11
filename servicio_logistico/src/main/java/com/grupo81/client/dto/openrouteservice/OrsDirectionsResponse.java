package com.grupo81.client.dto.openrouteservice;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrsDirectionsResponse(
    List<OrsFeature> features
) {}