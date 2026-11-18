package com.ulima.incidenciaurbana.repository;

import com.ulima.incidenciaurbana.model.FotoArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FotoArchivoRepository extends JpaRepository<FotoArchivo, Long> {
    Optional<FotoArchivo> findByClave(String clave);

    void deleteByClaveIn(java.util.Collection<String> claves);
}
