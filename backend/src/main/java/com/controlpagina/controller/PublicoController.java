package com.controlpagina.controller;

import com.controlpagina.dto.CarruselResponse;
import com.controlpagina.dto.NoticiaResponse;
import com.controlpagina.dto.PDFDocumentoResponse;
import com.controlpagina.dto.TrabajadorPublicoResponse;
import com.controlpagina.service.CarruselService;
import com.controlpagina.service.NoticiaService;
import com.controlpagina.service.PDFDocumentoService;
import com.controlpagina.service.TrabajadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de solo lectura para el sitio público informativo.
 * Devuelven únicamente contenido activo.
 */
@RestController
@RequestMapping("/api/publico")
public class PublicoController {

    private final NoticiaService noticiaService;
    private final CarruselService carruselService;
    private final PDFDocumentoService pdfDocumentoService;
    private final TrabajadorService trabajadorService;

    public PublicoController(
            NoticiaService noticiaService,
            CarruselService carruselService,
            PDFDocumentoService pdfDocumentoService,
            TrabajadorService trabajadorService) {
        this.noticiaService = noticiaService;
        this.carruselService = carruselService;
        this.pdfDocumentoService = pdfDocumentoService;
        this.trabajadorService = trabajadorService;
    }

    @GetMapping("/noticias")
    public ResponseEntity<List<NoticiaResponse>> noticias() {
        return ResponseEntity.ok(noticiaService.findAllActivas());
    }

    @GetMapping("/noticias/recientes")
    public ResponseEntity<List<NoticiaResponse>> noticiasRecientes(
            @RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(noticiaService.findRecientesActivas(limit));
    }

    @GetMapping("/noticias/{slug}")
    public ResponseEntity<NoticiaResponse> noticia(@PathVariable String slug) {
        return noticiaService.findBySlug(slug)
                .filter(n -> Boolean.TRUE.equals(n.getActivo()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/carrusel")
    public ResponseEntity<List<CarruselResponse>> carrusel() {
        return ResponseEntity.ok(carruselService.findAllActivos());
    }

    @GetMapping("/pdf-documentos")
    public ResponseEntity<List<PDFDocumentoResponse>> pdfs() {
        return ResponseEntity.ok(pdfDocumentoService.findAllActivos());
    }

    @GetMapping("/trabajadores")
    public ResponseEntity<List<TrabajadorPublicoResponse>> trabajadores() {
        return ResponseEntity.ok(trabajadorService.findAllActivos());
    }
}
