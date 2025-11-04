package com.grupo81.servicio_logistico.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ContenedorSeguimientoDTO {
    private String identificacion;
    private String estadoActual;
    private String ubicacionActual;
}