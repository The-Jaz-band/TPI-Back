package com.grupo81.dtos.solicitud.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorSeguimientoDTO {
    private String identificacion;
    private String estadoActual;
    private String ubicacionActual;
}