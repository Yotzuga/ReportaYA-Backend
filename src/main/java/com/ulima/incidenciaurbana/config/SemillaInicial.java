package com.ulima.incidenciaurbana.config;

import com.ulima.incidenciaurbana.model.OperadorMunicipal;
import com.ulima.incidenciaurbana.model.Persona;
import com.ulima.incidenciaurbana.model.Tecnico;
import com.ulima.incidenciaurbana.repository.CuentaRepository;
import com.ulima.incidenciaurbana.repository.OperadorMunicipalRepository;
import com.ulima.incidenciaurbana.repository.TecnicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el operador y los tecnicos iniciales en una base vacia (por ejemplo, un
 * despliegue nuevo en la nube), sin scripts SQL manuales.
 *
 * <p>Solo actua si se configuran los hashes BCrypt en
 * APP_SEED_OPERADOR_HASH / APP_SEED_TECNICO_HASH y todavia no existe ningun
 * operador / tecnico. Recibe hashes, no contrasenas: la clave en texto plano
 * nunca pasa por la configuracion del servidor. Es idempotente.
 */
@Component
@Order(10)
public class SemillaInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SemillaInicial.class);

    private final OperadorMunicipalRepository operadores;
    private final TecnicoRepository tecnicos;
    private final CuentaRepository cuentas;
    private final String hashOperador;
    private final String hashTecnico;

    public SemillaInicial(OperadorMunicipalRepository operadores,
                          TecnicoRepository tecnicos,
                          CuentaRepository cuentas,
                          @Value("${app.seed.operador-hash:}") String hashOperador,
                          @Value("${app.seed.tecnico-hash:}") String hashTecnico) {
        this.operadores = operadores;
        this.tecnicos = tecnicos;
        this.cuentas = cuentas;
        this.hashOperador = hashOperador == null ? "" : hashOperador.trim();
        this.hashTecnico = hashTecnico == null ? "" : hashTecnico.trim();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (esBcrypt(hashOperador) && operadores.count() == 0) {
            crearOperador("op.maria", "Maria", "Rojas", "09887766", "955443322", "maria.rojas@muni.gob.pe");
            log.info("Semilla: operador inicial creado (op.maria)");
        }
        if (esBcrypt(hashTecnico) && tecnicos.count() == 0) {
            crearTecnico("tec.carlos", "Carlos", "Mendoza", "70123456", "933221100", "carlos.mendoza@muni.gob.pe");
            crearTecnico("tec.lucia", "Lucia", "Paredes", "70988111", "933000111", "lucia.paredes@muni.gob.pe");
            crearTecnico("tec.maria", "Maria", "Torres", "70555222", "933444555", "maria.torres@muni.gob.pe");
            log.info("Semilla: tecnicos iniciales creados (tec.carlos, tec.lucia, tec.maria)");
        }
    }

    private void crearOperador(String usuario, String nombres, String apellidos, String dni, String tel, String correo) {
        if (cuentas.existsByUsuarioIgnoreCase(usuario)) return;
        operadores.save(new OperadorMunicipal(usuario, hashOperador,
                new Persona(nombres, apellidos, dni, tel, correo)));
    }

    private void crearTecnico(String usuario, String nombres, String apellidos, String dni, String tel, String correo) {
        if (cuentas.existsByUsuarioIgnoreCase(usuario)) return;
        tecnicos.save(new Tecnico(usuario, hashTecnico,
                new Persona(nombres, apellidos, dni, tel, correo)));
    }

    private static boolean esBcrypt(String v) {
        return v.startsWith("$2a$") || v.startsWith("$2b$") || v.startsWith("$2y$");
    }
}
