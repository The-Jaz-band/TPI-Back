package com.grupo81.servicio_logistico.client.dto.googleMaps;

import java.util.List;

public record Route(
    List<Leg> legs
) {}
