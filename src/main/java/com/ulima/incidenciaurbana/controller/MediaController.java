package com.ulima.incidenciaurbana.controller;

import com.ulima.incidenciaurbana.repository.FotoArchivoRepository;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Sirve las fotos guardadas en la base de datos (modo sin Firebase).
 * Esta fuera de /api para que Image.network de la app las cargue sin JWT;
 * la clave es un UUID aleatorio, asi que no se pueden recorrer por id.
 */
@RestController
public class MediaController {

    private final FotoArchivoRepository archivoRepository;

    public MediaController(FotoArchivoRepository archivoRepository) {
        this.archivoRepository = archivoRepository;
    }

    @GetMapping("/media/fotos/{clave}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> obtener(@PathVariable String clave) {
        return archivoRepository.findByClave(clave)
                .map(a -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(a.getContentType()))
                        // La clave nunca cambia de contenido: se puede cachear mucho tiempo.
                        .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                        .body(a.getContenido()))
                .orElse(ResponseEntity.notFound().build());
    }
}
