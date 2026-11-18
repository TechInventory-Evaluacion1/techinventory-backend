package com.techinventory.catalogo.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.techinventory.catalogo.dto.KafkaStreamEvent;

@Component
public class CatalogoKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(CatalogoKafkaProducer.class);

    private final KafkaTemplate<String, KafkaStreamEvent> kafkaTemplate;

    @Value("${app.kafka.topics.catalogo-eventos.name}")
    private String catalogoEventosTopic;

    @Value("${app.kafka.topics.analitica-stock.name}")
    private String analiticaStockTopic;

    public CatalogoKafkaProducer(KafkaTemplate<String, KafkaStreamEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicarEventoCatalogo(KafkaStreamEvent event) {
        String key = event.getProductoId() != null ? String.valueOf(event.getProductoId()) : "general";
        kafkaTemplate.send(catalogoEventosTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[KAFKA-PRODUCER] Evento '{}' publicado en tópico '{}' (Partición: {}, Offset: {})",
                                event.getTipoEvento(),
                                catalogoEventosTopic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("[KAFKA-PRODUCER] Error publicando en tópico '{}': {}",
                                catalogoEventosTopic, ex.getMessage(), ex);
                    }
                });
    }

    public void publicarAnaliticaStock(KafkaStreamEvent event) {
        String key = event.getProductoId() != null ? String.valueOf(event.getProductoId()) : "metricas";
        kafkaTemplate.send(analiticaStockTopic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("[KAFKA-PRODUCER] Métrica '{}' enviada a tópico '{}' (Partición: {}, Offset: {})",
                                event.getTipoEvento(),
                                analiticaStockTopic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("[KAFKA-PRODUCER] Error enviando métrica a '{}': {}",
                                analiticaStockTopic, ex.getMessage(), ex);
                    }
                });
    }
}