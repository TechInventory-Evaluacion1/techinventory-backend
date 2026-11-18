package com.techinventory.inventario.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import com.techinventory.inventario.dto.KafkaStreamEvent;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        // Reintenta 2 veces con 1 segundo de espera antes de descartar y registrar el error
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> log.error(
                        "[KAFKA-ERROR-HANDLER] Mensaje defectuoso descartado tras reintentos. Tópico: {}, Partición: {}, Offset: {}, Causa: {}",
                        record.topic(), record.partition(), record.offset(), exception.getMessage()),
                new FixedBackOff(1000L, 2L)
        );
        // No reintentar errores de validación de payload (datos inválidos)
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaStreamEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, KafkaStreamEvent> consumerFactory,
            CommonErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, KafkaStreamEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }
}