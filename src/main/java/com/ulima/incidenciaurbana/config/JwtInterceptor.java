package com.ulima.incidenciaurbana.config;

import com.ulima.incidenciaurbana.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token no proporcionado\"}");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.parseClaims(token);
            request.setAttribute("cuentaId", Long.valueOf(claims.getSubject()));
            request.setAttribute("usuario", claims.get("usuario", String.class));
            request.setAttribute("tipoCuenta", claims.get("tipoCuenta", String.class));
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token invalido o expirado\"}");
            return false;
        }
    }
}
