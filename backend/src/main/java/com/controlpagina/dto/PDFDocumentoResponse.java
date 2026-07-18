package com.controlpagina.dto;

import com.controlpagina.entity.PDFDocumento;

public class PDFDocumentoResponse {

    private Long id;
    private String nombre;
    private String nombreOriginal;
    private String rutaArchivo;
    private String url;
    private Integer orden;
    private Boolean activo;
    private String createdAt;
    private String updatedAt;

    public static PDFDocumentoResponse fromEntity(PDFDocumento doc) {
        PDFDocumentoResponse dto = new PDFDocumentoResponse();
        dto.setId(doc.getId());
        dto.setNombre(doc.getNombre());
        dto.setNombreOriginal(doc.getNombreOriginal());
        dto.setRutaArchivo(doc.getRutaArchivo());
        dto.setUrl("/archivos/" + doc.getRutaArchivo());
        dto.setOrden(doc.getOrden());
        dto.setActivo(doc.getActivo());
        dto.setCreatedAt(doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : null);
        dto.setUpdatedAt(doc.getUpdatedAt() != null ? doc.getUpdatedAt().toString() : null);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
