package com.techinventory.inventario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaStreamEvent {
    private String eventId;
    private String tipoEvento;
    private Long productoId;
    private String nombreProducto;
    private String categoria;
    private BigDecimal precio;
    private Integer cantidad;
    private Integer stockResultante;
    private String origenServicio;
    private LocalDateTime timestamp;
}