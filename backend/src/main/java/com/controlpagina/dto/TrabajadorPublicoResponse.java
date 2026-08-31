package com.controlpagina.dto;

import com.controlpagina.entity.Trabajador;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DTO de consulta pública para el módulo de trabajadores accesible via QR.
 *
 * Solo expone los campos estrictamente necesarios para identificar al trabajador
 * sin revelar datos internos (ID de BD, timestamps, componentes individuales del nombre).
 */
public class TrabajadorPublicoResponse {

    /** Código público legible — no es el ID interno de la base de datos. */
    private String codigoPublico;

    /** Nombre completo construido como "Primer Apellido Segundo Apellido Primer Nombre Segundo Nombre". */
    private String nombreCompleto;

    /** URL relativa de la foto, ej: "/archivos/imagenesTrabajadores/foto.jpg" */
    private String fotoUrl;

    /** Indica si el trabajador está activo en la cooperativa. */
    private Boolean activo;

    public static TrabajadorPublicoResponse fromEntity(Trabajador t) {
        TrabajadorPublicoResponse dto = new TrabajadorPublicoResponse();
        dto.setCodigoPublico(t.getCodigoPublico());

        // Formato: APELLIDO1 APELLIDO2 NOMBRE1 NOMBRE2 (estilo carnet — todo en mayúsculas en el front)
        dto.setNombreCompleto(Stream.of(
                t.getPrimerApellido(),
                t.getSegundoApellido(),
                t.getPrimerNombre(),
                t.getSegundoNombre()
        ).filter(v -> v != null && !v.isBlank()).collect(Collectors.joining(" ")));

        dto.setFotoUrl("/archivos/" + t.getFotoUrl());
        dto.setActivo(t.getActivo());
        return dto;
    }

    public String getCodigoPublico() { return codigoPublico; }
    public void setCodigoPublico(String codigoPublico) { this.codigoPublico = codigoPublico; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
