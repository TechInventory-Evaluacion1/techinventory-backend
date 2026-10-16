package com.techinventory.inventario.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimientos_inventario")
@NoArgsConstructor
@Builder
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private String tipo;

    private String origenDestino;

    private LocalDateTime fechaHora;

    @PrePersist
    public void prePersist() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }

    public MovimientoInventario(Long id, Producto producto, Integer cantidad, String tipo, String origenDestino, LocalDateTime fechaHora) {
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
        this.tipo = tipo;
        this.origenDestino = origenDestino;
        this.fechaHora = fechaHora;
    }

    // Getters y Setters explícitos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getOrigenDestino() { return origenDestino; }
    public void setOrigenDestino(String origenDestino) { this.origenDestino = origenDestino; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}