package com.techinventory.inventario.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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