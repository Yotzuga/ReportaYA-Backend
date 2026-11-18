package com.ulima.incidenciaurbana.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final String[] corsOrigins;

    /**
     * @param corsOrigins patrones permitidos separados por coma (app.cors.allowed-origins).
     *        La app movil no usa CORS; esto solo aplica a la version web. Por defecto
     *        solo localhost; en produccion se configura con el dominio real.
     */
    public WebConfig(JwtInterceptor jwtInterceptor,
            @Value("${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}") String corsOrigins) {
        this.jwtInterceptor = jwtInterceptor;
        this.corsOrigins = java.util.Arrays.stream(corsOrigins.split(","))
                .map(String::trim).filter(o -> !o.isEmpty()).toArray(String[]::new);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/cuenta",
                        "/api/cuenta/verificar",
                        "/api/cuenta/reenviar-verificacion"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(corsOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
