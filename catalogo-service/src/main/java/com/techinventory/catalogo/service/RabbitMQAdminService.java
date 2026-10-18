package com.techinventory.catalogo.service;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQAdminService.class);

    private final AmqpAdmin amqpAdmin;

    @Value("${app.rabbitmq.queues.producto-crear}")
    private String queueProductoCrear;

    @Value("${app.rabbitmq.queues.producto-crear-dlq}")
    private String queueProductoCrearDlq;

    @Value("${app.rabbitmq.exchanges.direct}")
    private String directExchangeName;

    @Value("${app.rabbitmq.exchanges.dlx}")
    private String dlxExchangeName;

    @Value("${app.rabbitmq.routing-keys.producto-crear}")
    private String routingKeyProductoCrear;

    @Value("${app.rabbitmq.routing-keys.producto-crear-dlq}")
    private String routingKeyProductoCrearDlq;

    public RabbitMQAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public record QueueInfo(String queueName, int messageCount, int consumerCount, boolean exists) {}

    public QueueInfo obtenerEstadoCola(String queueName) {
        Properties props = amqpAdmin.getQueueProperties(queueName);
        if (props == null) {
            return new QueueInfo(queueName, 0, 0, false);
        }
        int messageCount = toInt(props.get("QUEUE_MESSAGE_COUNT"));
        int consumerCount = toInt(props.get("QUEUE_CONSUMER_COUNT"));
        return new QueueInfo(queueName, messageCount, consumerCount, true);
    }

    public List<QueueInfo> listarColasPricipales() {
        return List.of(
            obtenerEstadoCola(queueProductoCrear),
            obtenerEstadoCola(queueProductoCrearDlq)
        );
    }

    public String crearCola(String queueName, boolean durable) {
        Queue queue = durable
                ? QueueBuilder.durable(queueName).build()
                : QueueBuilder.nonDurable(queueName).build();
        String result = amqpAdmin.declareQueue(queue);
        log.info("Cola creada exitosamente: {}", queueName);
        return result;
    }

    public boolean eliminarCola(String queueName) {
        log.warn("Eliminando cola: {}", queueName);
        return amqpAdmin.deleteQueue(queueName);
    }

    public void purgarCola(String queueName) {
        log.warn("Purgando mensajes de la cola: {}", queueName);
        amqpAdmin.purgeQueue(queueName, false);
    }

    public List<Map<String, String>> listarExchanges() {
        return List.of(
            Map.of("name", directExchangeName, "type", "direct"),
            Map.of("name", dlxExchangeName, "type", "direct")
        );
    }

    public void crearExchangeDirect(String exchangeName) {
        DirectExchange exchange = ExchangeBuilder.directExchange(exchangeName).durable(true).build();
        amqpAdmin.declareExchange(exchange);
        log.info("Direct Exchange creado exitosamente: {}", exchangeName);
    }

    public void crearExchangeTopic(String exchangeName) {
        TopicExchange exchange = ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
        amqpAdmin.declareExchange(exchange);
        log.info("Topic Exchange creado exitosamente: {}", exchangeName);
    }

    public boolean eliminarExchange(String exchangeName) {
        log.warn("Eliminando exchange: {}", exchangeName);
        return amqpAdmin.deleteExchange(exchangeName);
    }

    public List<Map<String, String>> listarBindings() {
        return List.of(
            Map.of(
                "queue", queueProductoCrear,
                "exchange", directExchangeName,
                "routingKey", routingKeyProductoCrear
            ),
            Map.of(
                "queue", queueProductoCrearDlq,
                "exchange", dlxExchangeName,
                "routingKey", routingKeyProductoCrearDlq
            )
        );
    }

    public void crearBinding(String queueName, String exchangeName, String routingKey) {
        Binding binding = new Binding(
            queueName,
            Binding.DestinationType.QUEUE,
            exchangeName,
            routingKey,
            null
        );
        amqpAdmin.declareBinding(binding);
        log.info("Binding creado exitosamente entre Cola: '{}' y Exchange: '{}'", queueName, exchangeName);
    }

    public void eliminarBinding(String queueName, String exchangeName, String routingKey) {
        Binding binding = new Binding(
            queueName,
            Binding.DestinationType.QUEUE,
            exchangeName,
            routingKey,
            null
        );
        amqpAdmin.removeBinding(binding);
        log.warn("Binding eliminado entre Cola: '{}' y Exchange: '{}'", queueName, exchangeName);
    }

    private int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }
}