package com.techinventory.catalogo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.techinventory.catalogo.entity.Producto;
import com.techinventory.catalogo.service.CatalogoService;
import com.techinventory.catalogo.service.CatalogoService.ItemReserva;

@RestController
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.PATCH,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        })
@RequestMapping({"/api/v1/productos", "/api/productos", "/api/public/productos"})
public class ProductoController {

    private final CatalogoService catalogoService;

    public ProductoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(catalogoService.listarProductos());
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.crearProducto(producto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.obtenerPorId(id));
    }

    public record ReservaRequest(List<ItemReserva> items) {}

    @PostMapping("/reservas")
    public ResponseEntity<Map<String, String>> reservar(@RequestBody ReservaRequest request) {
        try {
            catalogoService.reservar(request.items());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Reserva realizada"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
        }
    }
}
