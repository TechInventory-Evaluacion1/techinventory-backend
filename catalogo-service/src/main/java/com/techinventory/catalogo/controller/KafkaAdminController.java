package com.techinventory.catalogo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techinventory.catalogo.dto.KafkaAdminDtos.ConsumerGroupInfo;
import com.techinventory.catalogo.dto.KafkaAdminDtos.CrearTopicoRequest;
import com.techinventory.catalogo.dto.KafkaAdminDtos.TopicoInfo;
import com.techinventory.catalogo.service.KafkaAdminService;

@RestController
@RequestMapping("/api/v1/kafka/admin")
public class KafkaAdminController {

    private final KafkaAdminService kafkaAdminService;

    public KafkaAdminController(KafkaAdminService kafkaAdminService) {
        this.kafkaAdminService = kafkaAdminService;
    }

    @GetMapping("/topics")
    public ResponseEntity<?> listarTopicos() {
        try {
            List<TopicoInfo> topicos = kafkaAdminService.listarTopicos();
            return ResponseEntity.ok(topicos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al listar tópicos: " + e.getMessage()));
        }
    }

    @PostMapping("/topics")
    public ResponseEntity<?> crearTopico(@RequestBody CrearTopicoRequest request) {
        try {
            int particiones = request.particiones() != null ? request.particiones() : 1;
            short replicas = request.replicas() != null ? request.replicas() : 1;
            Map<String, Object> res = kafkaAdminService.crearTopico(
                    request.nombre(), particiones, replicas, request.configuraciones());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al crear tópico: " + e.getMessage()));
        }
    }

    @GetMapping("/topics/{nombre}/configs")
    public ResponseEntity<?> obtenerConfiguracion(@PathVariable String nombre) {
        try {
            return ResponseEntity.ok(kafkaAdminService.obtenerConfiguracionTopico(nombre));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No se encontró el tópico o su configuración: " + e.getMessage()));
        }
    }

    @PutMapping("/topics/{nombre}/configs")
    public ResponseEntity<?> actualizarConfiguracion(
            @PathVariable String nombre,
            @RequestBody Map<String, String> configuraciones) {
        try {
            return ResponseEntity.ok(kafkaAdminService.actualizarConfiguracionTopico(nombre, configuraciones));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al actualizar configuración: " + e.getMessage()));
        }
    }

    @PutMapping("/topics/{nombre}/partitions/{total}")
    public ResponseEntity<?> aumentarParticiones(
            @PathVariable String nombre,
            @PathVariable int total) {
        try {
            return ResponseEntity.ok(kafkaAdminService.aumentarParticiones(nombre, total));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al aumentar particiones: " + e.getMessage()));
        }
    }

    @DeleteMapping("/topics/{nombre}")
    public ResponseEntity<?> eliminarTopico(@PathVariable String nombre) {
        try {
            return ResponseEntity.ok(kafkaAdminService.eliminarTopico(nombre));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al eliminar tópico: " + e.getMessage()));
        }
    }

    @GetMapping("/consumer-groups")
    public ResponseEntity<?> listarConsumerGroups() {
        try {
            List<ConsumerGroupInfo> groups = kafkaAdminService.listarConsumerGroups();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al listar consumer groups: " + e.getMessage()));
        }
    }
}