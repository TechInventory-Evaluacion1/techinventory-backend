package com.techinventory.inventario.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techinventory.inventario.entity.MovimientoInventario;
import com.techinventory.inventario.entity.Producto;
import com.techinventory.inventario.service.InventarioService;

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
@RequestMapping({"/api/v1/inventario", "/api/inventario"})
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodosLosProductos() {
        return ResponseEntity.ok(inventarioService.obtenerTodosLosProductos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerDetalleProducto(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.obtenerProductoPorId(id));
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = inventarioService.guardarProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        producto.setId(id);
        Producto productoActualizado = inventarioService.guardarProducto(producto);
        return ResponseEntity.ok(productoActualizado);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Producto> actualizarStock(@PathVariable Long id, @RequestBody StockRequest request) {
        Producto productoActualizado = inventarioService.actualizarStock(id, request.getStock());
        return ResponseEntity.ok(productoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        inventarioService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoInventario> registrarMovimiento(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            @RequestParam String tipo,
            @RequestParam(required = false, defaultValue = "Centro Distribución Principal") String ubicacion) {

        MovimientoInventario nuevoMovimiento = inventarioService.registrarMovimiento(productoId, cantidad, tipo, ubicacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMovimiento);
    }

    // Clase estática auxiliar sustituyendo el record para evitar incompatibilidades en el editor
    public static class StockRequest {
        private Integer stock;

        public StockRequest() {}

        public StockRequest(Integer stock) {
            this.stock = stock;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }
    }
}