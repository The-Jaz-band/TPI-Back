package com.grupo81.servicio_logistico.dtos.deposito.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositoCreateRequestDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    private String nombre;
    
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 500)
    private String direccion;
    
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitud;
    
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitud;
    
    @NotNull(message = "El costo de estadía diario es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo debe ser mayor o igual a 0")
    private BigDecimal costoEstadiaDiario;
}

// ============ RESPONSE DTOs ============

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositoResponseDTO {
    private UUID id;
    private String nombre;
    private String direccion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private BigDecimal costoEstadiaDiario;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorEnDepositoResponseDTO {
    private UUID contenedorId;
    private String identificacion;
    private UUID solicitudId;
    private String numeroSolicitud;
    private UUID depositoId;
    private String depositoNombre;
    private LocalDateTime fechaIngreso;
    private UUID proximoTramoId;
    private String proximoDestino;
    private Boolean camionAsignado;
}