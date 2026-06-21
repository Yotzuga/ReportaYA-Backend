package com.ulima.incidenciaurbana.service.impl;

import com.ulima.incidenciaurbana.dto.ReporteDTO;
import com.ulima.incidenciaurbana.model.*;
import com.ulima.incidenciaurbana.repository.CuentaRepository;
import com.ulima.incidenciaurbana.repository.ReporteRepository;
import com.ulima.incidenciaurbana.service.IOperadorService;
import com.ulima.incidenciaurbana.service.IReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OperadorServiceImpl implements IOperadorService {

    private final ReporteRepository reporteRepository;
    private final CuentaRepository cuentaRepository;
    private final IReporteService reporteService;

    @Autowired
    public OperadorServiceImpl(ReporteRepository reporteRepository,
            CuentaRepository cuentaRepository,
            IReporteService reporteService) {
        this.reporteRepository = reporteRepository;
        this.cuentaRepository = cuentaRepository;
        this.reporteService = reporteService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReporteDTO> obtenerReportesPorEstado(String estado, int page) {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado es requerido");
        }

        try {
            EstadoReporte estadoEnum = EstadoReporte.valueOf(estado.toUpperCase());
            int p = Math.max(0, page);
            return reporteService.obtenerTodosReportes(p, estadoEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado invalido: " + estado);
        }
    }

    @Override
    public ReporteDTO aceptarReporte(Long reporteId, Long operadorId) {
        validarOperador(operadorId);

        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + reporteId));

        if (reporte.getEstado() != EstadoReporte.PENDIENTE) {
            throw new RuntimeException(
                    "El reporte debe estar en estado PENDIENTE para aceptar. Estado actual: " + reporte.getEstado());
        }

        return reporteService.cambiarEstadoReporte(reporteId, EstadoReporte.REVISION);
    }

    @Override
    public ReporteDTO rechazarReporte(Long reporteId, Long operadorId, String motivo) {
        validarOperador(operadorId);

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new RuntimeException("El motivo de rechazo es obligatorio");
        }

        return reporteService.rechazarReporte(reporteId, motivo);
    }

    private void validarOperador(Long operadorId) {
        if (operadorId == null) {
            throw new IllegalArgumentException("operadorId es requerido");
        }
        Cuenta operador = cuentaRepository.findById(operadorId)
                .orElseThrow(() -> new RuntimeException("Operador no encontrado con id: " + operadorId));

        if (!(operador instanceof OperadorMunicipal)) {
            throw new RuntimeException("Solo operadores municipales pueden realizar esta accion");
        }
    }
}
