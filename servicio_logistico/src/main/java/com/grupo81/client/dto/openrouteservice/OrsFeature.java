package com.grupo81.client.dto.openrouteservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrsFeature(
    OrsProperties properties
) {}