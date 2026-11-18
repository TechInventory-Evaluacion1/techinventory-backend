package com.techinventory.catalogo.dto;

import java.util.Map;

public class KafkaAdminDtos {

    public record CrearTopicoRequest(
            String nombre,
            Integer particiones,
            Short replicas,
            Map<String, String> configuraciones) {}

    public record TopicoInfo(
            String nombre,
            int particiones,
            int replicas,
            boolean interno) {}

    public record ConsumerGroupInfo(
            String groupId,
            String estado,
            int miembrosActivos,
            String coordinador) {}
}