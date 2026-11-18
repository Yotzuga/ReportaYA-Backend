package com.ulima.incidenciaurbana.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Chequeo de salud publico (fuera de /api, sin token). Lo usan Render para
 * saber si el despliegue arranco y el ping que evita que el servicio gratis
 * se duerma. Hace un SELECT 1 para confirmar tambien la conexion a la base.
 */
@RestController
public class SaludController {

    private final JdbcTemplate jdbc;

    public SaludController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/salud")
    public ResponseEntity<Map<String, String>> salud() {
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of("estado", "ok", "base", "ok"));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of("estado", "degradado", "base", "sin conexion"));
        }
    }
}
