package com.ulima.incidenciaurbana.service.impl;

import com.ulima.incidenciaurbana.dto.FotoDTO;
import com.ulima.incidenciaurbana.model.Foto;
import com.ulima.incidenciaurbana.model.FotoArchivo;
import com.ulima.incidenciaurbana.model.Reporte;
import com.ulima.incidenciaurbana.model.TipoFoto;
import com.ulima.incidenciaurbana.repository.FotoArchivoRepository;
import com.ulima.incidenciaurbana.repository.FotoRepository;
import com.ulima.incidenciaurbana.repository.ReporteRepository;
import com.ulima.incidenciaurbana.service.IFirebaseStorageService;
import com.ulima.incidenciaurbana.service.IFotoService;
import com.ulima.incidenciaurbana.util.UrlPublica;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
public class FotoServiceImpl implements IFotoService {

    private final FotoRepository fotoRepository;
    private final ReporteRepository reporteRepository;
    private final IFirebaseStorageService firebaseStorageService;
    private final FotoArchivoRepository archivoRepository;

    /** Tope por foto (ya comprimida por la app: 1280 px, calidad 60). */
    private static final int MAX_BYTES = 5 * 1024 * 1024;

    @Autowired
    public FotoServiceImpl(FotoRepository fotoRepository,
            ReporteRepository reporteRepository,
            IFirebaseStorageService firebaseStorageService,
            FotoArchivoRepository archivoRepository) {
        this.fotoRepository = fotoRepository;
        this.reporteRepository = reporteRepository;
        this.firebaseStorageService = firebaseStorageService;
        this.archivoRepository = archivoRepository;
    }

    @Override
    public FotoDTO subirFoto(Long reporteId, String archivoBase64, TipoFoto tipo, String descripcion) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + reporteId));

        // Acepta tanto base64 puro como un data-URI ("data:image/png;base64,...."):
        // si viene el prefijo, se descarta y solo se decodifica la carga util.
        String base64Limpio = archivoBase64.contains(",")
                ? archivoBase64.substring(archivoBase64.indexOf(',') + 1)
                : archivoBase64;
        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(base64Limpio.replaceAll("\\s", ""));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("La foto no es un base64 valido");
        }
        if (decodedBytes.length == 0) throw new RuntimeException("La foto esta vacia");
        if (decodedBytes.length > MAX_BYTES) throw new RuntimeException("La foto supera el maximo de 5 MB");

        String extension = determinarExtension(decodedBytes);
        String clave = UUID.randomUUID().toString();
        String nombreArchivo = "fotos/reporte-" + reporteId + "-" + tipo + "-" + clave + "." + extension;

        String urlFoto;
        if (firebaseStorageService.isAvailable()) {
            urlFoto = firebaseStorageService.uploadFile(nombreArchivo, decodedBytes);
        } else {
            // Sin Firebase: la foto se guarda en PostgreSQL y se sirve en /media/fotos/{clave}.
            // Antes se escribia en ./uploads con una ruta relativa: en Render ese disco se
            // borra en cada despliegue y la app no podia abrir la ruta.
            archivoRepository.save(new FotoArchivo(clave, contentType(extension), decodedBytes));
            urlFoto = "/media/fotos/" + clave;
        }

        Foto foto = new Foto(reporte, urlFoto, tipo, descripcion);
        foto = fotoRepository.save(foto);

        return toDTO(foto);
    }

    private FotoDTO toDTO(Foto foto) {
        FotoDTO dto = new FotoDTO();
        dto.setId(foto.getId());
        dto.setReporteId(foto.getReporte().getId());
        dto.setUrl(UrlPublica.absoluta(foto.getUrl()));
        dto.setTipo(foto.getTipo());
        dto.setDescripcion(foto.getDescripcion());
        dto.setFechaCarga(foto.getFechaCarga());
        return dto;
    }

    private String determinarExtension(byte[] bytes) {
        if (bytes.length < 4) return "jpg";
        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) return "png";
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) return "jpg";
        if (bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46
                && bytes.length > 12 && bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50)
            return "webp";
        return "jpg";
    }

    private String contentType(String extension) {
        switch (extension) {
            case "png": return "image/png";
            case "webp": return "image/webp";
            default: return "image/jpeg";
        }
    }
}
