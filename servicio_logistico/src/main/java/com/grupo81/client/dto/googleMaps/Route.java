package com.grupo81.client.dto.googleMaps;

import java.util.List;

public record Route(
    List<Leg> legs
) {}
