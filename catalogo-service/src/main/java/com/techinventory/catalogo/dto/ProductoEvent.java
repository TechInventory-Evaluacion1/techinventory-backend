package com.techinventory.catalogo.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductoEvent implements Serializable {

    private Long id;
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private Integer stockInicial;
    private String evento; // "PRODUCTO_CREADO"

    // Constructor vacío
    public ProductoEvent() {
    }

    // Constructor completo
    public ProductoEvent(Long id, String nombre, String categoria, BigDecimal precio, Integer stockInicial, String evento) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stockInicial = stockInicial;
        this.evento = evento;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStockInicial() {
        return stockInicial;
    }

    public void setStockInicial(Integer stockInicial) {
        this.stockInicial = stockInicial;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }
}