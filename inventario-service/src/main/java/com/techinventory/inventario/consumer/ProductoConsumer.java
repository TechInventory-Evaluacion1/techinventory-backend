package com.techinventory.inventario.consumer;

import com.rabbitmq.client.Channel;
import com.techinventory.inventario.dto.ProductoEvent;
import com.techinventory.inventario.service.InventarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProductoConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductoConsumer.class);

    private final InventarioService inventarioService;

    public ProductoConsumer(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.producto-crear}")
    public void consumirProductoCreado(ProductoEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Mensaje recibido desde la cola RabbitMQ: {}", event);
        
        try {
            // Inicialización e incremento del stock usando la lógica de tu InventarioService
            inventarioService.inicializarDesdeEvento(event);
            
            // Confirmación manual (ACK)
            channel.basicAck(tag, false);
            log.info("Mensaje procesado y ACK enviado correctamente para tag: {}", tag);
        } catch (Exception e) {
            log.error("Error al procesar el mensaje. Enviando NACK para descarte/DLQ. Error: {}", e.getMessage());
            
            // Rechazo manual sin reencolar (requeue = false) para enviar a la DLQ
            channel.basicNack(tag, false, false);
        }
    }
}