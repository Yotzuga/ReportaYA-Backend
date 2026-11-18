package com.ulima.incidenciaurbana.service.impl;

import com.ulima.incidenciaurbana.dto.LoginRequest;
import com.ulima.incidenciaurbana.dto.LoginResponse;
import com.ulima.incidenciaurbana.exception.EmailNotVerifiedException;
import com.ulima.incidenciaurbana.exception.InvalidCredentialsException;
import com.ulima.incidenciaurbana.model.Cuenta;
import com.ulima.incidenciaurbana.repository.CuentaRepository;
import com.ulima.incidenciaurbana.service.AuthService;
import com.ulima.incidenciaurbana.service.IEmailService;
import com.ulima.incidenciaurbana.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MINUTOS_VALIDEZ_RECUPERACION = 30;
    // Codigo corto de recuperacion (Opcion B): el usuario lo escribe en la app.
    // Alfabeto sin caracteres ambiguos (0/O, 1/I/L) para que sea facil de leer.
    private static final String ALFABETO_CODIGO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int LONGITUD_CODIGO = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CuentaRepository cuentaRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IEmailService emailService;
    private final String hashFicticio;

    public AuthServiceImpl(CuentaRepository cuentaRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           IEmailService emailService) {
        this.cuentaRepository = cuentaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.hashFicticio = passwordEncoder.encode("usuario-inexistente");
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Cuenta c = cuentaRepository.findByUsuario(request.getUsuario()).orElse(null);
        if (c == null) {
            // Mismo costo que una contrasena incorrecta: no revela si el usuario existe.
            passwordEncoder.matches(request.getPassword(), hashFicticio);
            throw new InvalidCredentialsException();
        }

        // Solo se aceptan hashes BCrypt. Las contrasenas en texto plano heredadas
        // (datos semilla antiguos) las convierte PasswordMigration al arrancar.
        String stored = c.getContrasenaHash();
        if (stored == null || !isBcryptHash(stored)
                || !passwordEncoder.matches(request.getPassword(), stored)) {
            throw new InvalidCredentialsException();
        }

        // Credenciales correctas pero la cuenta aún no verificó su correo.
        // Se comprueba DESPUÉS del password para no revelar cuentas por el usuario.
        if (!c.isActivo()) {
            throw new EmailNotVerifiedException();
        }

        String nombre = (c.getPersona() != null) ? c.getPersona().getNombreCompleto() : "";
        String token = jwtUtil.generateToken(c.getId(), c.getUsuario(), c.getTipoCuenta());
        return new LoginResponse(c.getId(), c.getUsuario(), nombre, "Login exitoso", c.getTipoCuenta(), token);
    }

    @Override
    @Transactional
    public void solicitarRecuperacion(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new RuntimeException("El correo es obligatorio");
        }

        // Respuesta uniforme: si el correo no existe no se informa (anti-enumeracion),
        // igual que dice el mensaje del controlador.
        Cuenta cuenta = cuentaRepository.findByCorreoAndActivoTrue(correo.trim()).orElse(null);
        if (cuenta == null) return;

        String token = generarCodigoRecuperacion();
        cuenta.asignarTokenRecuperacion(token, LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ_RECUPERACION));
        cuentaRepository.save(cuenta);

        emailService.enviarRecuperacionContrasena(
                cuenta.getPersona().getCorreo(),
                cuenta.getPersona().getNombres(),
                token);
    }

    @Override
    @Transactional
    public void restablecerContrasena(String token, String nuevaContrasena) {
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("El token de recuperacion es obligatorio");
        }
        if (nuevaContrasena == null || nuevaContrasena.length() < 6) {
            throw new RuntimeException("La contrasena debe tener al menos 6 caracteres");
        }

        Cuenta cuenta = cuentaRepository.findByTokenRecuperacion(token.trim())
                .orElseThrow(() -> new RuntimeException("Codigo de recuperacion invalido o ya utilizado"));

        if (cuenta.tokenRecuperacionExpirado()) {
            cuenta.limpiarTokenRecuperacion();
            cuentaRepository.save(cuenta);
            throw new RuntimeException("El codigo de recuperacion ha expirado. Solicita uno nuevo.");
        }

        cuenta.cambiarContrasena(passwordEncoder.encode(nuevaContrasena));
        cuenta.limpiarTokenRecuperacion();
        cuentaRepository.save(cuenta);
    }

    // Codigo alfanumerico corto para el correo de recuperacion (Opcion B).
    private String generarCodigoRecuperacion() {
        StringBuilder sb = new StringBuilder(LONGITUD_CODIGO);
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            sb.append(ALFABETO_CODIGO.charAt(RANDOM.nextInt(ALFABETO_CODIGO.length())));
        }
        return sb.toString();
    }

    static boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
