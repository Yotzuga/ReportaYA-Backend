package com.ulima.incidenciaurbana.service;

import com.ulima.incidenciaurbana.dto.ReporteDTO;
import com.ulima.incidenciaurbana.model.EstadoReporte;
import com.ulima.incidenciaurbana.model.TipoProblema;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IReporteService {
    ReporteDTO crearReporte(ReporteDTO reporteDTO);

    ReporteDTO obtenerReportePorId(Long id);

    Page<ReporteDTO> obtenerTodosReportes(int page, EstadoReporte estado);

    Page<ReporteDTO> obtenerReportesPorCuenta(Long cuentaId, int page);

    ReporteDTO actualizarReporte(Long id, ReporteDTO reporteDTO);

    ReporteDTO cambiarEstadoReporte(Long id, EstadoReporte nuevoEstado);

    ReporteDTO rechazarReporte(Long id, String motivo);

    void eliminarReporte(Long id);

    List<ReporteDTO> obtenerReportesMapa(EstadoReporte estado, TipoProblema tipo);
}
