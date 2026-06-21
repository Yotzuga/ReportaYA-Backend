package com.ulima.incidenciaurbana.service;

import com.ulima.incidenciaurbana.dto.ReporteDTO;
import org.springframework.data.domain.Page;

public interface IOperadorService {

    Page<ReporteDTO> obtenerReportesPorEstado(String estado, int page);

    ReporteDTO aceptarReporte(Long reporteId, Long operadorId);

    ReporteDTO rechazarReporte(Long reporteId, Long operadorId, String motivo);
}
