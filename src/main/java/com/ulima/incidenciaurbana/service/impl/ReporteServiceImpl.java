package com.ulima.incidenciaurbana.service.impl;

import com.ulima.incidenciaurbana.dto.ReporteDTO;
import com.ulima.incidenciaurbana.dto.UbicacionDTO;
import com.ulima.incidenciaurbana.dto.FotoDTO;
import com.ulima.incidenciaurbana.model.*;
import com.ulima.incidenciaurbana.repository.CuentaRepository;
import com.ulima.incidenciaurbana.repository.ReporteRepository;
import com.ulima.incidenciaurbana.repository.UbicacionRepository;
import com.ulima.incidenciaurbana.repository.HistorialEstadoRepository;
import com.ulima.incidenciaurbana.service.IReporteService;
import com.ulima.incidenciaurbana.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReporteServiceImpl implements IReporteService {

    private final ReporteRepository reporteRepository;
    private final CuentaRepository cuentaRepository;
    private final UbicacionRepository ubicacionRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final INotificationService notificationService;

    private static final int PAGE_SIZE = 10;

    @Autowired
    public ReporteServiceImpl(ReporteRepository reporteRepository,
            CuentaRepository cuentaRepository,
            UbicacionRepository ubicacionRepository,
            HistorialEstadoRepository historialEstadoRepository,
            INotificationService notificationService) {
        this.reporteRepository = reporteRepository;
        this.cuentaRepository = cuentaRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ReporteDTO crearReporte(ReporteDTO reporteDTO) {
        if (reporteDTO.getUbicacion() == null) {
            throw new RuntimeException("La ubicacion es obligatoria para crear un reporte");
        }
        if (reporteDTO.getUbicacion().getLatitud() == null || reporteDTO.getUbicacion().getLongitud() == null) {
            throw new RuntimeException("La latitud y longitud son obligatorias en la ubicacion");
        }
        if (reporteDTO.getCuentaId() == null) {
            throw new RuntimeException("El ID de la cuenta es obligatorio");
        }

        long cuentaId = reporteDTO.getCuentaId();
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con id: " + cuentaId));

        Reporte reporte = new Reporte(reporteDTO.getTitulo(), reporteDTO.getDescripcion(), cuenta);

        if (reporteDTO.getTipoProblema() != null) {
            reporte.setTipoProblema(reporteDTO.getTipoProblema());
        }

        Ubicacion ubicacion = new Ubicacion(
                reporteDTO.getUbicacion().getLatitud(),
                reporteDTO.getUbicacion().getLongitud(),
                reporteDTO.getUbicacion().getDireccion());
        ubicacion = ubicacionRepository.save(ubicacion);
        reporte.setUbicacion(ubicacion);

        cuenta.crearReporte(reporte);
        reporte = reporteRepository.save(reporte);

        HistorialEstado historial = new HistorialEstado(reporte, null, EstadoReporte.PENDIENTE);
        historialEstadoRepository.save(historial);

        return convertirADTO(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteDTO obtenerReportePorId(Long id) {
        if (id == null) {
            throw new RuntimeException("El ID del reporte es obligatorio");
        }
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + id));
        return convertirADTO(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReporteDTO> obtenerTodosReportes(int page, EstadoReporte estado) {
        int p = Math.max(0, page);
        PageRequest pageRequest = PageRequest.of(p, PAGE_SIZE, Sort.by("fechaCreacion").descending());

        if (estado != null) {
            return reporteRepository.findByEstado(estado, pageRequest).map(this::convertirADTO);
        }
        return reporteRepository.findAll(pageRequest).map(this::convertirADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReporteDTO> obtenerReportesPorCuenta(Long cuentaId, int page) {
        int p = Math.max(0, page);
        return reporteRepository
                .findByCuentaId(cuentaId, PageRequest.of(p, PAGE_SIZE, Sort.by("fechaCreacion").descending()))
                .map(this::convertirADTO);
    }

    @Override
    public ReporteDTO actualizarReporte(Long id, ReporteDTO reporteDTO) {
        if (id == null) {
            throw new RuntimeException("El ID del reporte es obligatorio");
        }
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + id));

        reporte.setTitulo(reporteDTO.getTitulo());
        reporte.setDescripcion(reporteDTO.getDescripcion());

        if (reporteDTO.getTipoProblema() != null) {
            reporte.setTipoProblema(reporteDTO.getTipoProblema());
        }

        if (reporteDTO.getUbicacion() == null) {
            throw new RuntimeException("La ubicacion es obligatoria");
        }
        if (reporteDTO.getUbicacion().getLatitud() == null || reporteDTO.getUbicacion().getLongitud() == null) {
            throw new RuntimeException("La latitud y longitud son obligatorias en la ubicacion");
        }

        Ubicacion ubicacion = reporte.getUbicacion();
        ubicacion.setLatitud(reporteDTO.getUbicacion().getLatitud());
        ubicacion.setLongitud(reporteDTO.getUbicacion().getLongitud());
        ubicacion.setDireccion(reporteDTO.getUbicacion().getDireccion());
        ubicacionRepository.save(ubicacion);

        reporte = reporteRepository.save(reporte);
        return convertirADTO(reporte);
    }

    @Override
    public ReporteDTO cambiarEstadoReporte(Long id, EstadoReporte nuevoEstado) {
        if (id == null) {
            throw new RuntimeException("El ID del reporte es obligatorio");
        }
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + id));

        EstadoReporte estadoAnterior = reporte.getEstado();
        if (estadoAnterior == nuevoEstado) {
            return convertirADTO(reporte);
        }

        reporte.cambiarEstado(nuevoEstado);
        reporte = reporteRepository.save(reporte);

        HistorialEstado historial = new HistorialEstado(reporte, estadoAnterior, nuevoEstado);
        historialEstadoRepository.save(historial);

        String mensaje = "El estado de tu reporte '" + reporte.getTitulo() + "' ha cambiado a " + nuevoEstado;
        notificationService.enviarNotificacion(reporte.getCuenta().getId(), "Actualizacion de Reporte", mensaje);

        return convertirADTO(reporte);
    }

    @Override
    public ReporteDTO rechazarReporte(Long id, String motivo) {
        if (id == null) {
            throw new RuntimeException("El ID del reporte es obligatorio");
        }
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + id));

        EstadoReporte estadoAnterior = reporte.getEstado();
        if (estadoAnterior == EstadoReporte.RECHAZADO) {
            return convertirADTO(reporte);
        }

        reporte.cambiarEstado(EstadoReporte.RECHAZADO);
        reporte.setComentarioResolucion(motivo);
        reporte.setFechaCierre(java.time.LocalDateTime.now());
        reporte = reporteRepository.save(reporte);

        HistorialEstado historial = new HistorialEstado(reporte, estadoAnterior, EstadoReporte.RECHAZADO);
        historialEstadoRepository.save(historial);

        String mensaje = "Tu reporte ha sido rechazado. Motivo: " + motivo;
        notificationService.enviarNotificacion(reporte.getCuenta().getId(), "Reporte Rechazado", mensaje);

        return convertirADTO(reporte);
    }

    @Override
    public void eliminarReporte(Long id) {
        if (id == null) {
            throw new RuntimeException("El ID del reporte es obligatorio");
        }
        if (!reporteRepository.existsById(id)) {
            throw new RuntimeException("Reporte no encontrado con id: " + id);
        }
        reporteRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteDTO> obtenerReportesMapa(EstadoReporte estado, TipoProblema tipo) {
        org.springframework.data.jpa.domain.Specification<Reporte> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipoProblema"), tipo));
            }

            root.fetch("ubicacion", jakarta.persistence.criteria.JoinType.LEFT);
            root.fetch("cuenta", jakarta.persistence.criteria.JoinType.LEFT);

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return reporteRepository.findAll(spec).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    private ReporteDTO convertirADTO(Reporte reporte) {
        ReporteDTO dto = new ReporteDTO();
        dto.setId(reporte.getId());
        dto.setTitulo(reporte.getTitulo());
        dto.setDescripcion(reporte.getDescripcion());
        dto.setCuentaId(reporte.getCuenta().getId());
        dto.setNombreCiudadano(reporte.getCuenta().getPersona().getNombreCompleto());
        dto.setEstado(reporte.getEstado());
        dto.setTipoProblema(reporte.getTipoProblema());
        dto.setFechaCreacion(reporte.getFechaCreacion());
        dto.setFechaActualizacion(reporte.getFechaActualizacion());
        dto.setComentarioResolucion(reporte.getComentarioResolucion());
        dto.setFechaCierre(reporte.getFechaCierre());

        if (reporte.getUbicacion() != null) {
            dto.setUbicacion(new UbicacionDTO(
                    reporte.getUbicacion().getId(),
                    reporte.getUbicacion().getLatitud(),
                    reporte.getUbicacion().getLongitud(),
                    reporte.getUbicacion().getDireccion(),
                    reporte.getUbicacion().getFechaRegistro()));
        }

        if (reporte.getFotos() != null && !reporte.getFotos().isEmpty()) {
            dto.setFotos(reporte.getFotos().stream()
                    .sorted((f1, f2) -> {
                        Map<TipoFoto, Integer> orden = Map.of(TipoFoto.INICIAL, 1, TipoFoto.FINAL, 2);
                        return orden.getOrDefault(f1.getTipo(), 99).compareTo(orden.getOrDefault(f2.getTipo(), 99));
                    })
                    .map(this::convertirFotoADTO)
                    .toList());
        }

        if (reporte.getTecnico() != null) {
            dto.setTecnicoAsignadoId(reporte.getTecnico().getId());
            dto.setTecnicoNombre(reporte.getTecnico().getPersona().getNombreCompleto());
        }

        return dto;
    }

    private FotoDTO convertirFotoADTO(Foto foto) {
        FotoDTO fotoDTO = new FotoDTO();
        fotoDTO.setId(foto.getId());
        fotoDTO.setReporteId(foto.getReporte().getId());
        fotoDTO.setUrl(foto.getUrl());
        fotoDTO.setTipo(foto.getTipo());
        fotoDTO.setDescripcion(foto.getDescripcion());
        fotoDTO.setFechaCarga(foto.getFechaCarga());
        return fotoDTO;
    }
}
