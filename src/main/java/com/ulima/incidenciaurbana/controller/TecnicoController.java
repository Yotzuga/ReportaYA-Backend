package com.ulima.incidenciaurbana.controller;

import com.ulima.incidenciaurbana.util.ApiErrores;
import com.ulima.incidenciaurbana.dto.CompletarReporteRequest;
import com.ulima.incidenciaurbana.dto.ReporteDTO;
import com.ulima.incidenciaurbana.dto.TecnicoDTO;
import com.ulima.incidenciaurbana.exception.ForbiddenException;
import com.ulima.incidenciaurbana.service.ITecnicoService;
import com.ulima.incidenciaurbana.util.RequestAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tecnicos")

public class TecnicoController {

    private final ITecnicoService tecnicoService;

    @Autowired
    public TecnicoController(ITecnicoService tecnicoService) {
        this.tecnicoService = tecnicoService;
    }

    @GetMapping
    public ResponseEntity<Page<TecnicoDTO>> obtenerTodosTecnicos(
            @RequestParam(name = "page", defaultValue = "0") int page,
            HttpServletRequest request) {
        // Lista para asignar: solo operadores.
        RequestAuth.requireRole(request, RequestAuth.OPERADOR);
        Page<TecnicoDTO> tecnicos = tecnicoService.obtenerTodosTecnicos(page);
        return ResponseEntity.ok(tecnicos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerTecnico(@PathVariable Long id, HttpServletRequest request) {
        RequestAuth.requireSelfOrRole(request, id, RequestAuth.OPERADOR);
        try {
            TecnicoDTO tecnico = tecnicoService.obtenerTecnicoPorId(id);
            return ResponseEntity.ok(tecnico);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ApiErrores.mensaje(e)));
        }
    }

    @GetMapping("/{id}/reportes")
    public ResponseEntity<?> obtenerReportesAsignados(
            @PathVariable Long id,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "page", defaultValue = "0") int page,
            HttpServletRequest request) {
        // Un tecnico solo ve sus asignaciones; el operador ve las de cualquiera.
        RequestAuth.requireSelfOrRole(request, id, RequestAuth.OPERADOR);
        try {
            return ResponseEntity.ok(tecnicoService.obtenerReportesAsignados(id, estado, page));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", ApiErrores.mensaje(e)));
        }
    }

    @PatchMapping("/{id}/reportes/{reporteId}/completar")
    public ResponseEntity<?> completarReporte(
            @PathVariable Long id,
            @PathVariable Long reporteId,
            @Valid @RequestBody CompletarReporteRequest request,
            HttpServletRequest httpRequest) {
        // Solo un TECNICO puede completar, y solo sus propios reportes: el id del
        // path debe coincidir con la cuenta del token (no se puede completar como otro).
        RequestAuth.requireRole(httpRequest, RequestAuth.TECNICO);
        Long actor = RequestAuth.cuentaId(httpRequest);
        if (actor == null || !actor.equals(id)) {
            throw new ForbiddenException("Solo el tecnico asignado puede completar este reporte.");
        }
        try {
            ReporteDTO reporteDTO = tecnicoService.completarReporte(id, reporteId, request);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Reporte completado exitosamente y marcado como FINALIZADO",
                    "reporte", reporteDTO,
                    "fotosAdjuntadas", request.getFotos().size(),
                    "estadoFinal", "FINALIZADO"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", ApiErrores.mensaje(e)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ApiErrores.mensaje(e)));
        }
    }
}
