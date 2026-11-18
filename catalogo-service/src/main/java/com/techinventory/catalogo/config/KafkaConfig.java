package com.techinventory.catalogo.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.catalogo-eventos.name}")
    private String catalogoEventosTopic;

    @Value("${app.kafka.topics.catalogo-eventos.partitions}")
    private int catalogoEventosPartitions;

    @Value("${app.kafka.topics.catalogo-eventos.replicas}")
    private short catalogoEventosReplicas;

    @Value("${app.kafka.topics.catalogo-eventos.retention-ms}")
    private String catalogoEventosRetention;

    @Value("${app.kafka.topics.catalogo-eventos.cleanup-policy}")
    private String catalogoEventosCleanup;

    @Value("${app.kafka.topics.analitica-stock.name}")
    private String analiticaStockTopic;

    @Value("${app.kafka.topics.analitica-stock.partitions}")
    private int analiticaStockPartitions;

    @Value("${app.kafka.topics.analitica-stock.replicas}")
    private short analiticaStockReplicas;

    @Value("${app.kafka.topics.analitica-stock.retention-ms}")
    private String analiticaStockRetention;

    @Value("${app.kafka.topics.analitica-stock.cleanup-policy}")
    private String analiticaStockCleanup;

    @Bean
    public NewTopic catalogoEventosTopic() {
        return TopicBuilder.name(catalogoEventosTopic)
                .partitions(catalogoEventosPartitions)
                .replicas(catalogoEventosReplicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, catalogoEventosRetention)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, catalogoEventosCleanup)
                .build();
    }

    @Bean
    public NewTopic analiticaStockTopic() {
        return TopicBuilder.name(analiticaStockTopic)
                .partitions(analiticaStockPartitions)
                .replicas(analiticaStockReplicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, analiticaStockRetention)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, analiticaStockCleanup)
                .build();
    }

    @Bean
    public AdminClient kafkaAdminClient() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return AdminClient.create(configs);
    }
}