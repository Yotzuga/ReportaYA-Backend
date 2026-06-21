package com.ulima.incidenciaurbana.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AsignacionDTO {

    private Long id;

    @NotNull(message = "El ID del reporte es obligatorio")
    private Long reporteId;

    @NotNull(message = "El ID del operador municipal es obligatorio")
    private Long operadorId;

    @NotNull(message = "El ID del tecnico es obligatorio")
    private Long tecnicoId;

    private String reporteTitulo;
    private String operadorNombre;
    private String tecnicoNombre;
    private LocalDateTime fechaAsignacion;

    public AsignacionDTO() {}

    public AsignacionDTO(Long reporteId, Long operadorId, Long tecnicoId) {
        this.reporteId = reporteId;
        this.operadorId = operadorId;
        this.tecnicoId = tecnicoId;
    }

    public AsignacionDTO(Long id, Long reporteId, Long operadorId, Long tecnicoId,
            String reporteTitulo, String operadorNombre, String tecnicoNombre,
            LocalDateTime fechaAsignacion) {
        this.id = id;
        this.reporteId = reporteId;
        this.operadorId = operadorId;
        this.tecnicoId = tecnicoId;
        this.reporteTitulo = reporteTitulo;
        this.operadorNombre = operadorNombre;
        this.tecnicoNombre = tecnicoNombre;
        this.fechaAsignacion = fechaAsignacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReporteId() { return reporteId; }
    public void setReporteId(Long reporteId) { this.reporteId = reporteId; }

    public Long getOperadorId() { return operadorId; }
    public void setOperadorId(Long operadorId) { this.operadorId = operadorId; }

    public Long getTecnicoId() { return tecnicoId; }
    public void setTecnicoId(Long tecnicoId) { this.tecnicoId = tecnicoId; }

    public String getReporteTitulo() { return reporteTitulo; }
    public void setReporteTitulo(String reporteTitulo) { this.reporteTitulo = reporteTitulo; }

    public String getOperadorNombre() { return operadorNombre; }
    public void setOperadorNombre(String operadorNombre) { this.operadorNombre = operadorNombre; }

    public String getTecnicoNombre() { return tecnicoNombre; }
    public void setTecnicoNombre(String tecnicoNombre) { this.tecnicoNombre = tecnicoNombre; }

    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
}
