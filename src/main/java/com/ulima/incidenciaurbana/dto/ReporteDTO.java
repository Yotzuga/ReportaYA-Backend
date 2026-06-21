package com.ulima.incidenciaurbana.dto;

import com.ulima.incidenciaurbana.model.EstadoReporte;
import com.ulima.incidenciaurbana.model.TipoProblema;
import java.time.LocalDateTime;
import java.util.List;

public class ReporteDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private Long cuentaId;
    private String nombreCiudadano;
    private EstadoReporte estado;
    private TipoProblema tipoProblema;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private UbicacionDTO ubicacion;
    private List<FotoDTO> fotos;
    private String comentarioResolucion;
    private LocalDateTime fechaCierre;
    private Long tecnicoAsignadoId;
    private String tecnicoNombre;

    public ReporteDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public String getNombreCiudadano() { return nombreCiudadano; }
    public void setNombreCiudadano(String nombreCiudadano) { this.nombreCiudadano = nombreCiudadano; }

    public EstadoReporte getEstado() { return estado; }
    public void setEstado(EstadoReporte estado) { this.estado = estado; }

    public TipoProblema getTipoProblema() { return tipoProblema; }
    public void setTipoProblema(TipoProblema tipoProblema) { this.tipoProblema = tipoProblema; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public UbicacionDTO getUbicacion() { return ubicacion; }
    public void setUbicacion(UbicacionDTO ubicacion) { this.ubicacion = ubicacion; }

    public List<FotoDTO> getFotos() { return fotos; }
    public void setFotos(List<FotoDTO> fotos) { this.fotos = fotos; }

    public String getComentarioResolucion() { return comentarioResolucion; }
    public void setComentarioResolucion(String comentarioResolucion) { this.comentarioResolucion = comentarioResolucion; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public Long getTecnicoAsignadoId() { return tecnicoAsignadoId; }
    public void setTecnicoAsignadoId(Long tecnicoAsignadoId) { this.tecnicoAsignadoId = tecnicoAsignadoId; }

    public String getTecnicoNombre() { return tecnicoNombre; }
    public void setTecnicoNombre(String tecnicoNombre) { this.tecnicoNombre = tecnicoNombre; }
}
