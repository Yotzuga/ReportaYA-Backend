package com.ulima.incidenciaurbana.service.impl;

import com.ulima.incidenciaurbana.dto.CompletarReporteRequest;
import com.ulima.incidenciaurbana.dto.ReporteDTO;
import com.ulima.incidenciaurbana.dto.TecnicoDTO;
import com.ulima.incidenciaurbana.model.*;
import com.ulima.incidenciaurbana.repository.FotoRepository;
import com.ulima.incidenciaurbana.repository.ReporteRepository;
import com.ulima.incidenciaurbana.repository.TecnicoRepository;
import com.ulima.incidenciaurbana.service.ITecnicoService;
import com.ulima.incidenciaurbana.service.IReporteService;
import com.ulima.incidenciaurbana.service.IFirebaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TecnicoServiceImpl implements ITecnicoService {

    private final ReporteRepository reporteRepository;
    private final FotoRepository fotoRepository;
    private final IReporteService reporteService;
    private final TecnicoRepository tecnicoRepository;
    private final IFirebaseStorageService firebaseStorageService;
    private static final int PAGE_SIZE = 20;

    @Autowired
    public TecnicoServiceImpl(ReporteRepository reporteRepository,
            FotoRepository fotoRepository,
            IReporteService reporteService,
            TecnicoRepository tecnicoRepository,
            IFirebaseStorageService firebaseStorageService) {
        this.reporteRepository = reporteRepository;
        this.fotoRepository = fotoRepository;
        this.reporteService = reporteService;
        this.tecnicoRepository = tecnicoRepository;
        this.firebaseStorageService = firebaseStorageService;
    }

    @Override
    public Page<TecnicoDTO> obtenerTodosTecnicos(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());
        Page<Tecnico> tecnicos = tecnicoRepository.findAll(pageable);

        return tecnicos.map(tecnico -> new TecnicoDTO(
                tecnico.getId(),
                tecnico.getUsuario(),
                tecnico.getPersona().getNombres(),
                tecnico.getPersona().getApellidos(),
                tecnico.getPersona().getCorreo(),
                null));
    }

    @Override
    public Page<ReporteDTO> obtenerReportesAsignados(Long tecnicoId, String estado, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());

        if (estado == null || estado.trim().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0L);
        }

        try {
            EstadoReporte estadoEnum = EstadoReporte.valueOf(estado.toUpperCase());
            Page<Reporte> reportes = reporteRepository.findByTecnicoIdAndEstado(tecnicoId, estadoEnum, pageable);

            List<ReporteDTO> reportesDTO = reportes.getContent().stream()
                    .map(r -> reporteService.obtenerReportePorId(r.getId()))
                    .collect(Collectors.toList());

            return new PageImpl<>(reportesDTO, pageable, reportes.getTotalElements());
        } catch (IllegalArgumentException e) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0L);
        }
    }

    @Override
    @Transactional
    public ReporteDTO completarReporte(Long tecnicoId, Long reporteId, CompletarReporteRequest request) {
        Objects.requireNonNull(reporteId, "reporteId no puede ser null");

        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado"));

        if (reporte.getEstado() != EstadoReporte.REVISION) {
            throw new IllegalArgumentException(
                    "El reporte debe estar en estado REVISION para completarlo. Estado actual: " + reporte.getEstado());
        }

        if (reporte.getTecnico() == null || !Objects.equals(reporte.getTecnico().getId(), tecnicoId)) {
            throw new IllegalArgumentException("Solo el tecnico asignado puede completar el reporte");
        }

        try {
            for (CompletarReporteRequest.FotoRequestInline fotoRequest : request.getFotos()) {
                byte[] decodedBytes = Base64.getDecoder().decode(fotoRequest.getArchivoBase64());
                String extension = determinarExtensionFoto(decodedBytes);
                String nombreArchivo = "fotos/reporte-" + reporteId + "-FINAL-"
                        + UUID.randomUUID() + "." + extension;

                String urlFoto;
                if (firebaseStorageService.isAvailable()) {
                    urlFoto = firebaseStorageService.uploadFile(nombreArchivo, decodedBytes);
                } else {
                    Path dirPath = Paths.get("uploads/fotos");
                    Files.createDirectories(dirPath);
                    Path filePath = dirPath.resolve(nombreArchivo);
                    Files.createDirectories(filePath.getParent());
                    Files.write(filePath, decodedBytes);
                    urlFoto = "uploads/fotos/" + nombreArchivo;
                }

                Foto foto = new Foto();
                foto.setReporte(reporte);
                foto.setUrl(urlFoto);
                foto.setTipo(TipoFoto.FINAL);
                foto.setDescripcion(fotoRequest.getDescripcion());
                foto.setFechaCarga(LocalDateTime.now());
                fotoRepository.save(foto);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar fotos: " + e.getMessage(), e);
        }

        reporte.setComentarioResolucion(request.getComentarioResolucion());
        reporte.setFechaCierre(LocalDateTime.now());
        reporte = reporteRepository.save(reporte);

        reporteService.cambiarEstadoReporte(reporte.getId(), EstadoReporte.FINALIZADO);

        return reporteService.obtenerReportePorId(reporteId);
    }

    private String determinarExtensionFoto(byte[] bytes) {
        if (bytes.length < 4) return "jpg";
        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) return "png";
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) return "jpg";
        if (bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46
                && bytes.length > 12 && bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50)
            return "webp";
        return "jpg";
    }
}
