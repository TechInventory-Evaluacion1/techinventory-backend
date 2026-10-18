package com.techinventory.catalogo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techinventory.catalogo.dto.ProductoEvent;
import com.techinventory.catalogo.entity.Producto;
import com.techinventory.catalogo.publisher.ProductoProducer;
import com.techinventory.catalogo.repository.ProductoRepository;

@Service
public class CatalogoService {

    public record ItemReserva(Long productoId, Integer cantidad) {}

    private final ProductoRepository productoRepository;
    private final ProductoProducer productoProducer;

    public CatalogoService(ProductoRepository productoRepository, ProductoProducer productoProducer) {
        this.productoRepository = productoRepository;
        this.productoProducer = productoProducer;
    }

    @Transactional
    public Producto crearProducto(Producto producto) {
        Producto guardado = productoRepository.save(producto);
        
        ProductoEvent evento = new ProductoEvent(
            guardado.getId(),
            guardado.getNombre(),
            guardado.getCategoria(),
            guardado.getPrecio(),
            guardado.getStock(),
            "PRODUCTO_CREADO"
        );

        productoProducer.publicarProductoCreado(evento);
        return guardado;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con el ID: " + id));
    }

    @Transactional
    public void reservar(List<ItemReserva> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La reserva no tiene productos");
        }

        for (ItemReserva item : items) {
            if (item.cantidad() == null || item.cantidad() <= 0) {
                throw new IllegalArgumentException("Cantidad inválida");
            }

            Producto producto = obtenerPorId(item.productoId());
            if (producto.getStock() < item.cantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - item.cantidad());
            productoRepository.save(producto);
        }
    }
}