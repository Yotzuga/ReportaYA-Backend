package com.ulima.incidenciaurbana.service.impl;

import com.ulima.incidenciaurbana.dto.ReporteDTO;
import com.ulima.incidenciaurbana.model.*;
import com.ulima.incidenciaurbana.repository.CuentaRepository;
import com.ulima.incidenciaurbana.repository.ReporteRepository;
import com.ulima.incidenciaurbana.repository.TecnicoRepository;
import com.ulima.incidenciaurbana.repository.UbicacionRepository;
import com.ulima.incidenciaurbana.repository.HistorialEstadoRepository;
import com.ulima.incidenciaurbana.service.IReporteService;
import com.ulima.incidenciaurbana.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReporteServiceImpl implements IReporteService {

    private final ReporteRepository reporteRepository;
    private final CuentaRepository cuentaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final INotificationService notificationService;
    private final ReporteQueryServiceImpl queryService;
    private final com.ulima.incidenciaurbana.repository.FotoArchivoRepository fotoArchivoRepository;

    @Autowired
    public ReporteServiceImpl(ReporteRepository reporteRepository,
            CuentaRepository cuentaRepository,
            TecnicoRepository tecnicoRepository,
            UbicacionRepository ubicacionRepository,
            HistorialEstadoRepository historialEstadoRepository,
            INotificationService notificationService,
            ReporteQueryServiceImpl queryService,
            com.ulima.incidenciaurbana.repository.FotoArchivoRepository fotoArchivoRepository) {
        this.reporteRepository = reporteRepository;
        this.cuentaRepository = cuentaRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.historialEstadoRepository = historialEstadoRepository;
        this.notificationService = notificationService;
        this.queryService = queryService;
        this.fotoArchivoRepository = fotoArchivoRepository;
    }

    @Override
    public ReporteDTO crearReporte(ReporteDTO reporteDTO) {
        validarTextos(reporteDTO);
        if (reporteDTO.getUbicacion() == null)
            throw new RuntimeException("La ubicacion es obligatoria para crear un reporte");
        if (reporteDTO.getUbicacion().getLatitud() == null || reporteDTO.getUbicacion().getLongitud() == null)
            throw new RuntimeException("La latitud y longitud son obligatorias en la ubicacion");
        if (reporteDTO.getCuentaId() == null)
            throw new RuntimeException("El ID de la cuenta es obligatorio");

        Cuenta cuenta = cuentaRepository.findById(reporteDTO.getCuentaId())
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con id: " + reporteDTO.getCuentaId()));

        Reporte reporte = new Reporte(reporteDTO.getTitulo(), reporteDTO.getDescripcion(), cuenta);

        if (reporteDTO.getTipoProblema() != null) {
            reporte.setTipoProblema(reporteDTO.getTipoProblema());
        }

        Ubicacion ubicacion = new Ubicacion(
                reporteDTO.getUbicacion().getLatitud(),
                reporteDTO.getUbicacion().getLongitud(),
                direccionOCoordenadas(reporteDTO));
        ubicacion = ubicacionRepository.save(ubicacion);
        reporte.setUbicacion(ubicacion);

        cuenta.crearReporte(reporte);
        reporte = reporteRepository.save(reporte);

        historialEstadoRepository.save(new HistorialEstado(reporte, null, EstadoReporte.PENDIENTE));

        return queryService.convertirADTO(reporte);
    }

    @Override
    public ReporteDTO actualizarReporte(Long id, ReporteDTO reporteDTO) {
        if (id == null)
            throw new RuntimeException("El ID del reporte es obligatorio");
        validarTextos(reporteDTO);
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + id));

        reporte.setTitulo(reporteDTO.getTitulo());
        reporte.setDescripcion(reporteDTO.getDescripcion());

        if (reporteDTO.getTipoProblema() != null) {
            reporte.setTipoProblema(reporteDTO.getTipoProblema());
        }

        if (reporteDTO.getUbicacion() == null)
            throw new RuntimeException("La ubicacion es obligatoria");
        if (reporteDTO.getUbicacion().getLatitud() == null || reporteDTO.getUbicacion().getLongitud() == null)
            throw new RuntimeException("La latitud y longitud son obligatorias en la ubicacion");

        Ubicacion ubicacion = reporte.getUbicacion();
        ubicacion.setLatitud(reporteDTO.getUbicacion().getLatitud());
        ubicacion.setLongitud(reporteDTO.getUbicacion().getLongitud());
        ubicacion.setDireccion(direccionOCoordenadas(reporteDTO));
        ubicacionRepository.save(ubicacion);

        reporte = reporteRepository.save(reporte);
        return queryService.convertirADTO(reporte);
    }

    @Override
    public ReporteDTO cambiarEstadoReporte(Long id, EstadoReporte nuevoEstado) {
        if (id == null)
            throw new RuntimeException("El ID del reporte es obligatorio");
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + id));

        EstadoReporte estadoAnterior = reporte.getEstado();
        if (estadoAnterior == nuevoEstado)
            return queryService.convertirADTO(reporte);
        validarTransicion(estadoAnterior, nuevoEstado);

        reporte.cambiarEstado(nuevoEstado);
        reporte = reporteRepository.save(reporte);

        historialEstadoRepository.save(new HistorialEstado(reporte, estadoAnterior, nuevoEstado));

        String mensaje = "El estado de tu reporte '" + reporte.getTitulo() + "' ha cambiado a " + nuevoEstado;
        notificationService.enviarNotificacion(reporte.getCuenta().getId(), "Actualizacion de Reporte", mensaje);

        return queryService.convertirADTO(reporte);
    }

    @Override
    public ReporteDTO rechazarReporte(Long id, String motivo) {
        if (id == null)
            throw new RuntimeException("El ID del reporte es obligatorio");
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + id));

        EstadoReporte estadoAnterior = reporte.getEstado();

        if (estadoAnterior == EstadoReporte.RECHAZADO)
            return queryService.convertirADTO(reporte);
        if (estadoAnterior == EstadoReporte.FINALIZADO)
            throw new RuntimeException("No se puede rechazar un reporte que ya fue FINALIZADO");
        reporte.cambiarEstado(EstadoReporte.RECHAZADO);
        reporte.setComentarioResolucion(motivo);
        reporte.setFechaCierre(java.time.LocalDateTime.now());
        reporte = reporteRepository.save(reporte);

        historialEstadoRepository.save(new HistorialEstado(reporte, estadoAnterior, EstadoReporte.RECHAZADO));

        String mensaje = "Tu reporte ha sido rechazado. Motivo: " + motivo;
        notificationService.enviarNotificacion(reporte.getCuenta().getId(), "Reporte Rechazado", mensaje);

        return queryService.convertirADTO(reporte);
    }

    @Override
    public ReporteDTO asignarTecnico(Long reporteId, Long tecnicoId) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + reporteId));

        if (reporte.getEstado() != EstadoReporte.REVISION) {
            throw new RuntimeException(
                    "El reporte debe estar en estado REVISION para asignar tecnico. Estado actual: "
                            + reporte.getEstado());
        }

        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Tecnico no encontrado con id: " + tecnicoId));

        reporte.setTecnico(tecnico);
        reporte = reporteRepository.save(reporte);

        notificationService.enviarNotificacion(reporte.getCuenta().getId(),
                "Tecnico Asignado",
                "Se ha asignado un tecnico a tu reporte '" + reporte.getTitulo() + "'");

        return queryService.convertirADTO(reporte);
    }

    @Override
    public void eliminarReporte(Long id) {
        if (id == null)
            throw new RuntimeException("El ID del reporte es obligatorio");
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + id));
        // Las fotos guardadas en PostgreSQL (/media/fotos/{clave}) no cuelgan del
        // reporte por FK: se borran aqui para no dejar archivos huerfanos.
        java.util.List<String> claves = reporte.getFotos().stream()
                .map(f -> f.getUrl())
                .filter(u -> u != null && u.startsWith("/media/fotos/"))
                .map(u -> u.substring("/media/fotos/".length()))
                .toList();
        if (!claves.isEmpty()) fotoArchivoRepository.deleteByClaveIn(claves);
        reporteRepository.delete(reporte);
    }

    /**
     * Ciclo de vida permitido:
     *   PENDIENTE -> REVISION (operador acepta)
     *   REVISION  -> FINALIZADO (tecnico completa)
     *   PENDIENTE/REVISION -> RECHAZADO solo via rechazarReporte (exige motivo).
     * FINALIZADO y RECHAZADO son estados terminales.
     */
    private void validarTransicion(EstadoReporte actual, EstadoReporte nuevo) {
        if (nuevo == EstadoReporte.RECHAZADO) {
            throw new RuntimeException("Para rechazar un reporte usa la accion Rechazar (requiere motivo)");
        }
        boolean ok = (actual == EstadoReporte.PENDIENTE && nuevo == EstadoReporte.REVISION)
                || (actual == EstadoReporte.REVISION && nuevo == EstadoReporte.FINALIZADO);
        if (!ok) {
            throw new RuntimeException("Transicion no permitida: " + actual + " -> " + nuevo);
        }
    }

    /** Evita que un texto vacio o demasiado largo llegue a la BD como error SQL (500). */
    private void validarTextos(ReporteDTO dto) {
        String titulo = dto.getTitulo() == null ? "" : dto.getTitulo().trim();
        String descripcion = dto.getDescripcion() == null ? "" : dto.getDescripcion().trim();
        if (titulo.isEmpty()) throw new RuntimeException("El titulo es obligatorio");
        if (descripcion.isEmpty()) throw new RuntimeException("La descripcion es obligatoria");
        if (titulo.length() > 255) throw new RuntimeException("El titulo no puede superar 255 caracteres");
        if (descripcion.length() > 1000) throw new RuntimeException("La descripcion no puede superar 1000 caracteres");
        dto.setTitulo(titulo);
        dto.setDescripcion(descripcion);
    }

    /**
     * La columna ubicaciones.direccion es NOT NULL. Si la app no alcanzo a obtener
     * la direccion (geocodificacion lenta o sin internet), se guardan las coordenadas.
     */
    private String direccionOCoordenadas(ReporteDTO dto) {
        String d = dto.getUbicacion().getDireccion();
        if (d != null && !d.isBlank()) {
            d = d.trim();
            return d.length() > 500 ? d.substring(0, 500) : d;
        }
        return String.format(java.util.Locale.US, "Ubicacion %.5f, %.5f",
                dto.getUbicacion().getLatitud(), dto.getUbicacion().getLongitud());
    }
}
