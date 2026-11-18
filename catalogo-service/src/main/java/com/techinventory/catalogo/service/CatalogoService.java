package com.techinventory.catalogo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techinventory.catalogo.dto.KafkaStreamEvent;
import com.techinventory.catalogo.dto.ProductoEvent;
import com.techinventory.catalogo.entity.Producto;
import com.techinventory.catalogo.publisher.CatalogoKafkaProducer;
import com.techinventory.catalogo.publisher.ProductoProducer;
import com.techinventory.catalogo.repository.ProductoRepository;

@Service
public class CatalogoService {

    public record ItemReserva(Long productoId, Integer cantidad) {}

    private final ProductoRepository productoRepository;
    private final ProductoProducer productoProducer;
    private final CatalogoKafkaProducer catalogoKafkaProducer;

    public CatalogoService(
            ProductoRepository productoRepository,
            ProductoProducer productoProducer,
            CatalogoKafkaProducer catalogoKafkaProducer) {
        this.productoRepository = productoRepository;
        this.productoProducer = productoProducer;
        this.catalogoKafkaProducer = catalogoKafkaProducer;
    }

    @Transactional
    public Producto crearProducto(Producto producto) {
        Producto guardado = productoRepository.save(producto);

        // 1. Flujo existente RabbitMQ (Intacto)
        ProductoEvent eventoRabbit = new ProductoEvent(
            guardado.getId(),
            guardado.getNombre(),
            guardado.getCategoria(),
            guardado.getPrecio(),
            guardado.getStock(),
            "PRODUCTO_CREADO"
        );
        productoProducer.publicarProductoCreado(eventoRabbit);

        // 2. Flujo Streaming & Analítica Kafka (Evaluación 3)
        KafkaStreamEvent eventoKafka = KafkaStreamEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .tipoEvento("PRODUCTO_CREADO_STREAM")
                .productoId(guardado.getId())
                .nombreProducto(guardado.getNombre())
                .categoria(guardado.getCategoria())
                .precio(guardado.getPrecio())
                .cantidad(guardado.getStock())
                .stockResultante(guardado.getStock())
                .origenServicio("catalogo-service")
                .timestamp(LocalDateTime.now())
                .build();

        catalogoKafkaProducer.publicarEventoCatalogo(eventoKafka);
        catalogoKafkaProducer.publicarAnaliticaStock(eventoKafka);

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
            Producto actualizado = productoRepository.save(producto);

            // Emitir evento de reserva/descuento de stock a Kafka
            KafkaStreamEvent reservaEvent = KafkaStreamEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .tipoEvento("STOCK_RESERVADO")
                    .productoId(actualizado.getId())
                    .nombreProducto(actualizado.getNombre())
                    .categoria(actualizado.getCategoria())
                    .precio(actualizado.getPrecio())
                    .cantidad(item.cantidad())
                    .stockResultante(actualizado.getStock())
                    .origenServicio("catalogo-service")
                    .timestamp(LocalDateTime.now())
                    .build();

            catalogoKafkaProducer.publicarEventoCatalogo(reservaEvent);
            catalogoKafkaProducer.publicarAnaliticaStock(reservaEvent);
        }
    }
}