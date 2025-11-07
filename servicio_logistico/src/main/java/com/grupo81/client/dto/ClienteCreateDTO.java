package com.grupo81.servicio_logistico.client.dto;

public record ClienteCreateDTO(
    String nombre,
    String email,
    String telefono,
    String empresa
) {}