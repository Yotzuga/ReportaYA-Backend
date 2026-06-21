package com.ulima.incidenciaurbana.service;

import com.ulima.incidenciaurbana.dto.CompletarReporteRequest;
import com.ulima.incidenciaurbana.dto.ReporteDTO;
import com.ulima.incidenciaurbana.dto.TecnicoDTO;
import org.springframework.data.domain.Page;

public interface ITecnicoService {

    Page<TecnicoDTO> obtenerTodosTecnicos(int page);

    Page<ReporteDTO> obtenerReportesAsignados(Long tecnicoId, String estado, int page);

    ReporteDTO completarReporte(Long tecnicoId, Long reporteId, CompletarReporteRequest request);
}
