package com.techinventory.catalogo.config;



import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchanges.direct}")
    private String directExchangeName;

    @Value("${app.rabbitmq.exchanges.topic}")
    private String topicExchangeName;

    @Value("${app.rabbitmq.exchanges.dlx}")
    private String dlxExchangeName;

    @Value("${app.rabbitmq.queues.producto-crear}")
    private String queueName;

    @Value("${app.rabbitmq.queues.producto-crear-dlq}")
    private String dlqQueueName;

    @Value("${app.rabbitmq.routing-keys.producto-crear}")
    private String routingKey;

    @Value("${app.rabbitmq.routing-keys.producto-crear-dlq}")
    private String dlqRoutingKey;

    // 1. Convertidor JSON para serializar/deserializar objetos automáticamente en los mensajes
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    // 2. Exchanges (Direct, Topic y DLX)
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(directExchangeName, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(topicExchangeName, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    // 3. Cola Principal vinculada a la Dead Letter Exchange (DLX)
    @Bean
    public Queue productoCrearQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    // 4. Dead Letter Queue (DLQ) para mensajes no entregados/fallidos
    @Bean
    public Queue productoCrearDLQ() {
        return QueueBuilder.durable(dlqQueueName).build();
    }

    // 5. Enrutamiento (Binding): Cola Principal <-> Direct Exchange
    @Bean
    public Binding bindingProductoCrear() {
        return BindingBuilder.bind(productoCrearQueue())
                .to(directExchange())
                .with(routingKey);
    }

    // 6. Enrutamiento (Binding): DLQ <-> Dead Letter Exchange (DLX)
    @Bean
    public Binding bindingDLQ() {
        return BindingBuilder.bind(productoCrearDLQ())
                .to(deadLetterExchange())
                .with(dlqRoutingKey);
    }

    // Cola 2: notificaciones, con su DLQ
    @Bean
    public Queue notificacionQueue() {
        return QueueBuilder.durable("inventario.notificacion.queue")
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", "inventario.notificacion.key")
                .build();
    }

    @Bean
    public Queue notificacionDLQ() {
        return QueueBuilder.durable("inventario.notificacion.dlq").build();
    }

    @Bean
    public Binding bindingNotificacion() {
        return BindingBuilder.bind(notificacionQueue())
                .to(directExchange())
                .with("inventario.notificacion.key");
    }

    @Bean
    public Binding bindingNotificacionDLQ() {
        return BindingBuilder.bind(notificacionDLQ())
                .to(deadLetterExchange())
                .with("inventario.notificacion.key");
    }

    // Cola 3: stock, con su DLQ
    @Bean
    public Queue stockQueue() {
        return QueueBuilder.durable("inventario.stock.queue")
                .withArgument("x-dead-letter-exchange", dlxExchangeName)
                .withArgument("x-dead-letter-routing-key", "inventario.stock.key")
                .build();
    }

    @Bean
    public Queue stockDLQ() {
        return QueueBuilder.durable("inventario.stock.dlq").build();
    }

    @Bean
    public Binding bindingStockDLQ() {
        return BindingBuilder.bind(stockDLQ())
                .to(deadLetterExchange())
                .with("inventario.stock.key");
    }

    // La cola de stock escucha el topic con comodín (inventario.stock.bajo, inventario.stock.critico, etc.)
    @Bean
    public Binding bindingTopicStock() {
        return BindingBuilder.bind(stockQueue())
                .to(topicExchange())
                .with("inventario.stock.#");
    }
}