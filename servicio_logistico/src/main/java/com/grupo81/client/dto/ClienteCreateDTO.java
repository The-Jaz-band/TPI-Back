package com.grupo81.client.dto;

public record ClienteCreateDTO(
    String nombre,
    String email,
    String telefono,
    String empresa
) {}