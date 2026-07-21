package com.ulima.incidenciaurbana.repository;

import com.ulima.incidenciaurbana.model.Foto;
import com.ulima.incidenciaurbana.model.TipoFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {

    List<Foto> findByReporte_IdOrderByFechaCargaAsc(Long reporteId);

    List<Foto> findByReporte_IdAndTipoOrderByFechaCargaAsc(Long reporteId, TipoFoto tipo);
}
