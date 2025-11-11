package com.grupo81.client.dto.openrouteservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrsProperties(
    OrsSummary summary
) {}