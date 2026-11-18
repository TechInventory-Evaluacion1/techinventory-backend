package com.techinventory.inventario.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.techinventory.inventario.dto.KafkaStreamEvent;

@Component
public class InventarioKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventarioKafkaConsumer.class);

    @KafkaListener(
            topics = "${app.kafka.topics.catalogo-eventos}",
            groupId = "${app.kafka.groups.catalogo}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirEventosCatalogo(
            @Payload KafkaStreamEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        validarEvento(event);

        log.info("[KAFKA-CONSUMER][Group: inventario-catalogo-group] Tópico: {}, Partición: {}, Offset: {} | Evento: {} | Producto ID: {} ({}) | Stock resultante: {}",
                topic, partition, offset,
                event.getTipoEvento(),
                event.getProductoId(),
                event.getNombreProducto(),
                event.getStockResultante());
    }

    @KafkaListener(
            topics = "${app.kafka.topics.analitica-stock}",
            groupId = "${app.kafka.groups.analitica}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirAnaliticaStock(
            @Payload KafkaStreamEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        validarEvento(event);

        boolean alertaStockBajo = event.getStockResultante() != null && event.getStockResultante() <= 5;

        log.info("[KAFKA-ANALITICA][Group: inventario-analitica-group] Tópico: {}, Partición: {}, Offset: {} | Tipo: {} | Producto: {} | Cantidad movida: {} | Alerta Stock Bajo: {}",
                topic, partition, offset,
                event.getTipoEvento(),
                event.getNombreProducto(),
                event.getCantidad(),
                alertaStockBajo ? "SI (Stock crítico: " + event.getStockResultante() + ")" : "NO");
    }

    private void validarEvento(KafkaStreamEvent event) {
        if (event == null || event.getEventId() == null || event.getTipoEvento() == null) {
            throw new IllegalArgumentException("Payload de Kafka inválido: faltan eventId o tipoEvento");
        }
        if (event.getProductoId() == null) {
            throw new IllegalArgumentException("Payload de Kafka inválido: productoId es obligatorio");
        }
    }
}