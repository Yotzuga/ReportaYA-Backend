package com.ulima.incidenciaurbana.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Convierte rutas propias del backend ("/media/fotos/...") en URL absolutas
 * usando el host con el que el cliente llamo (IP de la PC, dominio de Render...).
 * Asi la URL no queda atada a una IP guardada en la base de datos.
 * Con server.forward-headers-strategy=framework respeta https detras de un proxy.
 */
public final class UrlPublica {

    private UrlPublica() {}

    public static String absoluta(String url) {
        if (url == null || !url.startsWith("/")) return url;
        if (RequestContextHolder.getRequestAttributes() == null) return url;
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(url).toUriString();
    }
}
