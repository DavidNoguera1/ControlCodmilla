package com.controlpagina.dto;

import com.controlpagina.entity.Trabajador;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrabajadorResponse {

    private Long id;
    private String codigoPublico;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String nombreCompleto;
    private String fotoUrl;
    private Boolean activo;
    private String createdAt;
    private String updatedAt;

    public static TrabajadorResponse fromEntity(Trabajador t) {
        TrabajadorResponse dto = new TrabajadorResponse();
        dto.setId(t.getId());
        dto.setCodigoPublico(t.getCodigoPublico());
        dto.setPrimerNombre(t.getPrimerNombre());
        dto.setSegundoNombre(t.getSegundoNombre());
        dto.setPrimerApellido(t.getPrimerApellido());
        dto.setSegundoApellido(t.getSegundoApellido());
        dto.setNombreCompleto(Stream.of(
                t.getPrimerNombre(),
                t.getSegundoNombre(),
                t.getPrimerApellido(),
                t.getSegundoApellido()
        ).filter(value -> value != null && !value.isBlank()).collect(Collectors.joining(" ")));
        dto.setFotoUrl("/archivos/" + t.getFotoUrl());
        dto.setActivo(t.getActivo());
        dto.setCreatedAt(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
        dto.setUpdatedAt(t.getUpdatedAt() != null ? t.getUpdatedAt().toString() : null);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoPublico() { return codigoPublico; }
    public void setCodigoPublico(String codigoPublico) { this.codigoPublico = codigoPublico; }
    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }
    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }
    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }
    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
