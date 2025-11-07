package com.grupo81.servicio_logistico.client.dto;

import java.util.UUID;

public record ClienteDTO(
    UUID id,
    String nombre,
    String email,
    String telefono,
    String empresa
) {}