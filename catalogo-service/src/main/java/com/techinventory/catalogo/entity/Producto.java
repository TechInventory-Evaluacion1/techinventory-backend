package com.techinventory.catalogo.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String sku;

    @Column(nullable = false)
    private String nombre;

    private String categoria;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    private String codigoBarras;
    private String ubicacionBodega;

    @Column(name = "url_imagen", length = 1000)
    private String urlImagen;

    public Producto(Long id, String sku, String nombre, String categoria, BigDecimal precio, Integer stock, String codigoBarras, String ubicacionBodega, String urlImagen) {
        this.id = id;
        this.sku = sku;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.codigoBarras = codigoBarras;
        this.ubicacionBodega = ubicacionBodega;
        this.urlImagen = urlImagen;
    }
}