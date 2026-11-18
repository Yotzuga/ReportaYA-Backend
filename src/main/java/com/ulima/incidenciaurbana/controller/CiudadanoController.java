package com.ulima.incidenciaurbana.controller;

import com.ulima.incidenciaurbana.util.ApiErrores;
import com.ulima.incidenciaurbana.dto.CiudadanoDTO;
import com.ulima.incidenciaurbana.service.ICiudadanoService;
import com.ulima.incidenciaurbana.util.RequestAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ciudadanos")
public class CiudadanoController {

    private final ICiudadanoService ciudadanoService;

    @Autowired
    public CiudadanoController(ICiudadanoService ciudadanoService) {
        this.ciudadanoService = ciudadanoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCiudadano(@PathVariable Long id, HttpServletRequest request) {
        // Solo el propio ciudadano o un operador.
        RequestAuth.requireSelfOrRole(request, id, RequestAuth.OPERADOR);
        try {
            CiudadanoDTO ciudadano = ciudadanoService.obtenerCiudadanoPorId(id);
            return ResponseEntity.ok(ciudadano);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ApiErrores.mensaje(e)));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCiudadano(
            @PathVariable Long id,
            @RequestBody CiudadanoDTO ciudadanoDTO,
            HttpServletRequest request) {
        RequestAuth.requireSelfOrRole(request, id, RequestAuth.OPERADOR);
        try {
            CiudadanoDTO actualizado = ciudadanoService.actualizarCiudadano(id, ciudadanoDTO);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ApiErrores.mensaje(e)));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCiudadano(@PathVariable Long id, HttpServletRequest request) {
        // Borrar cuentas es tarea administrativa.
        RequestAuth.requireRole(request, RequestAuth.OPERADOR);
        try {
            ciudadanoService.eliminarCiudadano(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ApiErrores.mensaje(e)));
        }
    }
}
