package com.techinventory.catalogo.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.stereotype.Service;

import com.techinventory.catalogo.dto.KafkaAdminDtos.ConsumerGroupInfo;
import com.techinventory.catalogo.dto.KafkaAdminDtos.TopicoInfo;

@Service
public class KafkaAdminService {

    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public List<TopicoInfo> listarTopicos() throws ExecutionException, InterruptedException {
        Collection<String> names = adminClient.listTopics().names().get();
        Map<String, TopicDescription> descriptions = adminClient.describeTopics(names).allTopicNames().get();

        List<TopicoInfo> resultado = new ArrayList<>();
        for (TopicDescription desc : descriptions.values()) {
            int replicas = desc.partitions().isEmpty() ? 0 : desc.partitions().get(0).replicas().size();
            resultado.add(new TopicoInfo(
                    desc.name(),
                    desc.partitions().size(),
                    replicas,
                    desc.isInternal()
            ));
        }
        return resultado;
    }

    public Map<String, Object> crearTopico(String nombre, int particiones, short replicas, Map<String, String> configs)
            throws ExecutionException, InterruptedException {
        NewTopic newTopic = new NewTopic(nombre, particiones, replicas);
        if (configs != null && !configs.isEmpty()) {
            newTopic.configs(configs);
        }
        adminClient.createTopics(Collections.singletonList(newTopic)).all().get();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Tópico creado exitosamente");
        respuesta.put("topico", nombre);
        respuesta.put("particiones", particiones);
        respuesta.put("replicas", replicas);
        return respuesta;
    }

    public Map<String, Object> obtenerConfiguracionTopico(String nombre)
            throws ExecutionException, InterruptedException {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Config config = adminClient.describeConfigs(Collections.singletonList(resource)).all().get().get(resource);

        Map<String, String> propiedades = new HashMap<>();
        for (ConfigEntry entry : config.entries()) {
            if (!entry.isDefault()) {
                propiedades.put(entry.name(), entry.value());
            }
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("topico", nombre);
        respuesta.put("configuracionesPersonalizadas", propiedades);
        return respuesta;
    }

    public Map<String, Object> actualizarConfiguracionTopico(String nombre, Map<String, String> nuevasConfigs)
            throws ExecutionException, InterruptedException {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        List<AlterConfigOp> ops = nuevasConfigs.entrySet().stream()
                .map(e -> new AlterConfigOp(new ConfigEntry(e.getKey(), e.getValue()), AlterConfigOp.OpType.SET))
                .collect(Collectors.toList());

        adminClient.incrementalAlterConfigs(Collections.singletonMap(resource, ops)).all().get();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Configuración actualizada exitosamente");
        respuesta.put("topico", nombre);
        respuesta.put("cambios", nuevasConfigs);
        return respuesta;
    }

    public Map<String, Object> aumentarParticiones(String nombre, int totalParticiones)
            throws ExecutionException, InterruptedException {
        adminClient.createPartitions(Collections.singletonMap(nombre, NewPartitions.increaseTo(totalParticiones)))
                .all().get();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Particiones actualizadas");
        respuesta.put("topico", nombre);
        respuesta.put("totalParticiones", totalParticiones);
        return respuesta;
    }

    public Map<String, Object> eliminarTopico(String nombre) throws ExecutionException, InterruptedException {
        adminClient.deleteTopics(Collections.singletonList(nombre)).all().get();
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Tópico eliminado exitosamente");
        respuesta.put("topico", nombre);
        return respuesta;
    }

    public List<ConsumerGroupInfo> listarConsumerGroups() throws ExecutionException, InterruptedException {
        Collection<ConsumerGroupListing> listings = adminClient.listConsumerGroups().all().get();
        List<String> groupIds = listings.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList());

        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, ConsumerGroupDescription> descriptions = adminClient.describeConsumerGroups(groupIds).all().get();

        List<ConsumerGroupInfo> resultado = new ArrayList<>();
        for (ConsumerGroupDescription desc : descriptions.values()) {
            String coordinador = desc.coordinator() != null ? desc.coordinator().host() : "N/A";
            resultado.add(new ConsumerGroupInfo(
                    desc.groupId(),
                    desc.state().toString(),
                    desc.members().size(),
                    coordinador
            ));
        }
        return resultado;
    }
}