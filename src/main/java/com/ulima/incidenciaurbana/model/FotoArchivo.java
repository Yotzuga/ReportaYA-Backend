package com.ulima.incidenciaurbana.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Contenido binario de una foto cuando Firebase Storage no esta configurado.
 *
 * Se guarda en PostgreSQL (bytea) para que las fotos sobrevivan reinicios y
 * redespliegues (el disco de Render es efimero). Esta en una tabla aparte de
 * {@link Foto} para que listar reportes no cargue los bytes de cada imagen.
 * Se sirve en GET /media/fotos/{clave}; la clave es un UUID no adivinable.
 */
@Entity
@Table(name = "foto_archivos")
public class FotoArchivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String clave;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(nullable = false)
    private byte[] contenido;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga = LocalDateTime.now();

    public FotoArchivo() {
    }

    public FotoArchivo(String clave, String contentType, byte[] contenido) {
        this.clave = clave;
        this.contentType = contentType;
        this.contenido = contenido;
    }

    public Long getId() { return id; }
    public String getClave() { return clave; }
    public String getContentType() { return contentType; }
    public byte[] getContenido() { return contenido; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }
}
