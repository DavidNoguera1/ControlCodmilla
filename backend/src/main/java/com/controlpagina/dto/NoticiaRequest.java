package com.controlpagina.dto;

import jakarta.validation.constraints.NotBlank;

public class NoticiaRequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String slug;

    private String contenido;

    private String imagenPortada;

    private Boolean activo;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public String getImagenPortada() { return imagenPortada; }
    public void setImagenPortada(String imagenPortada) { this.imagenPortada = imagenPortada; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
