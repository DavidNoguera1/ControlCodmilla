package com.controlpagina.dto;

import com.controlpagina.entity.Carrusel;

public class CarruselResponse {

    private Long id;
    private String titulo;
    private String imagenUrl;
    private String linkUrl;
    private Integer orden;
    private Boolean activo;
    private String createdAt;
    private String updatedAt;

    public static CarruselResponse fromEntity(Carrusel c) {
        CarruselResponse dto = new CarruselResponse();
        dto.setId(c.getId());
        dto.setTitulo(c.getTitulo());
        dto.setImagenUrl("/archivos/" + c.getImagenUrl());
        dto.setLinkUrl(c.getLinkUrl());
        dto.setOrden(c.getOrden());
        dto.setActivo(c.getActivo());
        dto.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
        dto.setUpdatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
