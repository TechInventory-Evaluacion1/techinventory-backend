package com.techinventory.inventario.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techinventory.inventario.dto.ProductoEvent;
import com.techinventory.inventario.entity.MovimientoInventario;
import com.techinventory.inventario.entity.Producto;
import com.techinventory.inventario.repository.MovimientoInventarioRepository;
import com.techinventory.inventario.repository.ProductoRepository;

@Service
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public InventarioService(ProductoRepository productoRepository, MovimientoInventarioRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    public Producto obtenerProductoPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con el ID: " + id));
    }

    @Transactional
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto inicializarDesdeEvento(ProductoEvent event) {
        if (event == null || event.getNombre() == null || event.getNombre().isBlank()) {
            throw new IllegalArgumentException("El evento recibido no contiene un nombre de producto válido");
        }
        if (event.getId() == null) {
            throw new IllegalArgumentException("El evento recibido no contiene el id del producto");
        }

        Producto producto = productoRepository.findById(event.getId()).orElseGet(Producto::new);
        producto.setId(event.getId());
        producto.setNombre(event.getNombre());
        producto.setCategoria(event.getCategoria());
        
        if (event.getPrecio() != null) {
            producto.setPrecio(event.getPrecio());
        } else {
            producto.setPrecio(BigDecimal.ZERO);
        }

        if (event.getStockInicial() != null) {
            producto.setStock(event.getStockInicial());
        } else {
            producto.setStock(0);
        }

        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    @Transactional
    public Producto actualizarStock(Long id, Integer nuevoStock) {
        Producto producto = obtenerProductoPorId(id);
        producto.setStock(nuevoStock);
        return productoRepository.save(producto);
    }

    @Transactional
    public MovimientoInventario registrarMovimiento(Long productoId, Integer cantidad, String tipo, String ubicacion) {
        Producto producto = obtenerProductoPorId(productoId);

        if ("SALIDA".equalsIgnoreCase(tipo) || "DESPACHO".equalsIgnoreCase(tipo)) {
            if (producto.getStock() < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre());
            }
            producto.setStock(producto.getStock() - cantidad);
        } else if ("ENTRADA".equalsIgnoreCase(tipo)) {
            producto.setStock(producto.getStock() + cantidad);
        } else {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + tipo);
        }

        productoRepository.save(producto);

        MovimientoInventario movimiento = new MovimientoInventario(
                null,
                producto,
                cantidad,
                tipo.toUpperCase(),
                ubicacion,
                null
        );

        return movimientoRepository.save(movimiento);
    }
}