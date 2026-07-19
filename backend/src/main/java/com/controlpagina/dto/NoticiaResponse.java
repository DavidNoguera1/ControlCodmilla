package com.controlpagina.dto;

import com.controlpagina.entity.Noticia;
import java.time.LocalDateTime;

public class NoticiaResponse {

    private Long id;
    private String titulo;
    private String slug;
    private String contenido;
    private String imagenPortada;
    private String fechaPublicacion;
    private Boolean activo;
    private Boolean destacado;

    public static NoticiaResponse fromEntity(Noticia noticia) {
        NoticiaResponse dto = new NoticiaResponse();
        dto.setId(noticia.getId());
        dto.setTitulo(noticia.getTitulo());
        dto.setSlug(noticia.getSlug());
        dto.setContenido(noticia.getContenido());
        dto.setImagenPortada(noticia.getImagenPortada());
        dto.setFechaPublicacion(noticia.getFechaPublicacion() != null
                ? noticia.getFechaPublicacion().toString()
                : null);
        dto.setActivo(noticia.getActivo());
        dto.setDestacado(noticia.getDestacado());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public String getImagenPortada() { return imagenPortada; }
    public void setImagenPortada(String imagenPortada) { this.imagenPortada = imagenPortada; }
    public String getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(String fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public Boolean getDestacado() { return destacado; }
    public void setDestacado(Boolean destacado) { this.destacado = destacado; }
}
