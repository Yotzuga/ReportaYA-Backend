package com.ulima.incidenciaurbana.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "operadores_municipales")
public class OperadorMunicipal extends Cuenta {

    public OperadorMunicipal() {
        super();
    }

    public OperadorMunicipal(String usuario, String contrasenaHash, Persona persona) {
        super(usuario, contrasenaHash, persona);
    }

    @Override
    public String getTipoCuenta() {
        return "OPERADOR_MUNICIPAL";
    }
}
