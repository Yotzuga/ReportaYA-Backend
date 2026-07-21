package com.ulima.incidenciaurbana.service;

import com.ulima.incidenciaurbana.dto.FotoDTO;
import com.ulima.incidenciaurbana.model.TipoFoto;

import java.util.List;

public interface IFotoService {

    FotoDTO subirFoto(Long reporteId, String archivoBase64, TipoFoto tipo, String descripcion);

    /**
     * Lista las fotos de un reporte. Si {@code tipo} es null, devuelve todas
     * (INICIAL y FINAL); si no, solo las del tipo indicado. Orden por fecha de carga.
     */
    List<FotoDTO> listarFotosPorReporte(Long reporteId, TipoFoto tipo);
}
