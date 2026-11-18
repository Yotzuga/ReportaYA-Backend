package com.ulima.incidenciaurbana.util;

import org.springframework.dao.DataAccessException;

/**
 * Mensaje de error seguro para el cliente. Los errores de base de datos
 * (restricciones, SQL) se reemplazan por un texto generico: no deben llegar a
 * la app ni exponer nombres de tablas o consultas.
 */
public final class ApiErrores {

    private ApiErrores() {}

    public static String mensaje(Exception e) {
        if (e instanceof DataAccessException) {
            return "Los datos no son validos o ya existen. Revisa la informacion enviada.";
        }
        String m = e.getMessage();
        return (m == null || m.isBlank()) ? "No se pudo procesar la solicitud." : m;
    }
}
