package com.ulima.incidenciaurbana.service.impl;

import com.ulima.incidenciaurbana.dto.AsignacionDTO;
import com.ulima.incidenciaurbana.model.*;
import com.ulima.incidenciaurbana.repository.CuentaRepository;
import com.ulima.incidenciaurbana.repository.ReporteRepository;
import com.ulima.incidenciaurbana.service.IAsignacionService;
import com.ulima.incidenciaurbana.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AsignacionServiceImpl implements IAsignacionService {

    private final ReporteRepository reporteRepository;
    private final CuentaRepository cuentaRepository;
    private final INotificationService notificationService;

    @Autowired
    public AsignacionServiceImpl(ReporteRepository reporteRepository,
            CuentaRepository cuentaRepository,
            INotificationService notificationService) {
        this.reporteRepository = reporteRepository;
        this.cuentaRepository = cuentaRepository;
        this.notificationService = notificationService;
    }

    @Override
    public AsignacionDTO crearAsignacion(AsignacionDTO asignacionDTO) {
        Reporte reporte = reporteRepository.findById(asignacionDTO.getReporteId())
                .orElseThrow(() -> new RuntimeException(
                        "Reporte no encontrado con id: " + asignacionDTO.getReporteId()));

        if (reporte.getEstado() != EstadoReporte.REVISION) {
            throw new RuntimeException(
                    "El reporte debe estar en estado REVISION para poder asignar un tecnico. Estado actual: "
                            + reporte.getEstado());
        }

        Cuenta operadorCuenta = cuentaRepository.findById(asignacionDTO.getOperadorId())
                .orElseThrow(() -> new RuntimeException(
                        "Operador no encontrado con id: " + asignacionDTO.getOperadorId()));

        if (!(operadorCuenta instanceof OperadorMunicipal)) {
            throw new RuntimeException(
                    "El usuario con id " + asignacionDTO.getOperadorId() + " no es un operador municipal");
        }

        Cuenta tecnicoCuenta = cuentaRepository.findById(asignacionDTO.getTecnicoId())
                .orElseThrow(() -> new RuntimeException(
                        "Tecnico no encontrado con id: " + asignacionDTO.getTecnicoId()));

        if (!(tecnicoCuenta instanceof Tecnico)) {
            throw new RuntimeException(
                    "El usuario con id " + asignacionDTO.getTecnicoId() + " no es un tecnico");
        }

        Tecnico tecnico = (Tecnico) tecnicoCuenta;

        reporte.setTecnico(tecnico);
        reporte = reporteRepository.save(reporte);

        notificationService.enviarNotificacion(reporte.getCuenta().getId(),
                "Tecnico Asignado",
                "Se ha asignado un tecnico a tu reporte '" + reporte.getTitulo() + "'");

        return new AsignacionDTO(
                null,
                reporte.getId(),
                asignacionDTO.getOperadorId(),
                tecnico.getId(),
                reporte.getTitulo(),
                operadorCuenta.getPersona().getNombreCompleto(),
                tecnico.getPersona().getNombreCompleto(),
                java.time.LocalDateTime.now());
    }
}
