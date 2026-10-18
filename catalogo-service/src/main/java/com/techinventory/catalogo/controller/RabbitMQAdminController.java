package com.techinventory.catalogo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techinventory.catalogo.service.RabbitMQAdminService;

@RestController
@RequestMapping("/api/v1/admin")
public class RabbitMQAdminController {

    private final RabbitMQAdminService adminService;

    public RabbitMQAdminController(RabbitMQAdminService adminService) {
        this.adminService = adminService;
    }

    // --- ENDPOINTS DE COLAS (QUEUES) ---

    @GetMapping("/queues")
    public ResponseEntity<List<RabbitMQAdminService.QueueInfo>> listarColas() {
        return ResponseEntity.ok(adminService.listarColasPricipales());
    }

    @GetMapping("/queues/{queueName}")
    public ResponseEntity<RabbitMQAdminService.QueueInfo> obtenerEstadoCola(@PathVariable String queueName) {
        return ResponseEntity.ok(adminService.obtenerEstadoCola(queueName));
    }

    @PostMapping("/queues")
    public ResponseEntity<String> crearCola(
            @RequestParam(required = false) String queueName,
            @RequestParam(defaultValue = "true") boolean durable) {
        
        // Validación directa exigida por el Punto 7 (evita nombres vacíos o nulos -> 400 BAD REQUEST)
        if (queueName == null || queueName.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: El nombre de la cola es obligatorio y no puede estar vacío.");
        }

        String res = adminService.crearCola(queueName, durable);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cola declarada: " + res);
    }

    @DeleteMapping("/queues/{queueName}")
    public ResponseEntity<Void> eliminarCola(@PathVariable String queueName) {
        adminService.eliminarCola(queueName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/queues/{queueName}/purge")
    public ResponseEntity<Void> purgarCola(@PathVariable String queueName) {
        adminService.purgarCola(queueName);
        return ResponseEntity.ok().build();
    }

    // --- ENDPOINTS DE EXCHANGES ---

    @GetMapping("/exchanges")
    public ResponseEntity<List<Map<String, String>>> listarExchanges() {
        return ResponseEntity.ok(adminService.listarExchanges());
    }

    @PostMapping("/exchanges/direct")
    public ResponseEntity<String> crearExchangeDirect(@RequestParam(required = false) String exchangeName) {
        if (exchangeName == null || exchangeName.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: El nombre del exchange es obligatorio.");
        }
        adminService.crearExchangeDirect(exchangeName);
        return ResponseEntity.status(HttpStatus.CREATED).body("Exchange Direct creado: " + exchangeName);
    }

    @PostMapping("/exchanges/topic")
    public ResponseEntity<String> crearExchangeTopic(@RequestParam(required = false) String exchangeName) {
        if (exchangeName == null || exchangeName.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: El nombre del exchange es obligatorio.");
        }
        adminService.crearExchangeTopic(exchangeName);
        return ResponseEntity.status(HttpStatus.CREATED).body("Exchange Topic creado: " + exchangeName);
    }

    @DeleteMapping("/exchanges/{exchangeName}")
    public ResponseEntity<Void> eliminarExchange(@PathVariable String exchangeName) {
        adminService.eliminarExchange(exchangeName);
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINTS DE BINDINGS ---

    @GetMapping("/bindings")
    public ResponseEntity<List<Map<String, String>>> listarBindings() {
        return ResponseEntity.ok(adminService.listarBindings());
    }

    @PostMapping("/bindings")
    public ResponseEntity<String> crearBinding(
            @RequestParam(required = false) String queueName,
            @RequestParam(required = false) String exchangeName,
            @RequestParam(required = false) String routingKey) {
        
        if (queueName == null || queueName.trim().isEmpty() ||
            exchangeName == null || exchangeName.trim().isEmpty() ||
            routingKey == null || routingKey.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Todos los parámetros (queueName, exchangeName, routingKey) son obligatorios.");
        }

        adminService.crearBinding(queueName, exchangeName, routingKey);
        return ResponseEntity.status(HttpStatus.CREATED).body("Binding creado exitosamente.");
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(
            @RequestParam String queueName,
            @RequestParam String exchangeName,
            @RequestParam String routingKey) {
        adminService.eliminarBinding(queueName, exchangeName, routingKey);
        return ResponseEntity.noContent().build();
    }
}