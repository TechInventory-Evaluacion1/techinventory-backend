package com.techinventory.catalogo.publisher;

import com.techinventory.catalogo.dto.ProductoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductoProducer {

    private static final Logger log = LoggerFactory.getLogger(ProductoProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchanges.direct}")
    private String directExchange;

    @Value("${app.rabbitmq.routing-keys.producto-crear}")
    private String routingKey;

    public ProductoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarProductoCreado(ProductoEvent event) {
        log.info("Publicando evento de producto creado hacia RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(directExchange, routingKey, event);
    }
}