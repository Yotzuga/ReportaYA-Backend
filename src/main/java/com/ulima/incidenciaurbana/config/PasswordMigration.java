package com.ulima.incidenciaurbana.config;

import com.ulima.incidenciaurbana.model.Cuenta;
import com.ulima.incidenciaurbana.repository.CuentaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Al arrancar, convierte a BCrypt cualquier contrasena guardada en texto plano
 * (por ejemplo, de un script semilla antiguo). Asi el login ya no necesita
 * aceptar texto plano y la base nunca queda con claves legibles.
 * Es idempotente: en arranques siguientes no encuentra nada que migrar.
 */
@Component
public class PasswordMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigration.class);

    private final CuentaRepository cuentaRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordMigration(CuentaRepository cuentaRepository, BCryptPasswordEncoder passwordEncoder) {
        this.cuentaRepository = cuentaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Cuenta> cuentas = cuentaRepository.findAll();
        int migradas = 0;
        for (Cuenta c : cuentas) {
            String actual = c.getContrasenaHash();
            if (actual != null && !actual.isEmpty() && !esBcrypt(actual)) {
                c.setContrasenaHash(passwordEncoder.encode(actual));
                migradas++;
            }
        }
        if (migradas > 0) {
            cuentaRepository.saveAll(cuentas);
            log.info("Contrasenas en texto plano migradas a BCrypt: {}", migradas);
        }
    }

    private static boolean esBcrypt(String v) {
        return v.startsWith("$2a$") || v.startsWith("$2b$") || v.startsWith("$2y$");
    }
}
