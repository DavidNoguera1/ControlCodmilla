package com.controlpagina.controller;

import com.controlpagina.dto.PDFDocumentoRequest;
import com.controlpagina.dto.PDFDocumentoResponse;
import com.controlpagina.service.PDFDocumentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pdf-documentos")
public class PDFDocumentoController {

    private final PDFDocumentoService pdfDocumentoService;

    public PDFDocumentoController(PDFDocumentoService pdfDocumentoService) {
        this.pdfDocumentoService = pdfDocumentoService;
    }

    @GetMapping
    public ResponseEntity<List<PDFDocumentoResponse>> findAll() {
        return ResponseEntity.ok(pdfDocumentoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PDFDocumentoResponse> findById(@PathVariable Long id) {
        return pdfDocumentoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "orden", required = false) Integer orden,
            @RequestParam(value = "activo", required = false) Boolean activo) throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacío"));
        }

        String originalName = file.getOriginalFilename();
        PDFDocumentoRequest request = new PDFDocumentoRequest();
        String nombreDocumento = nombre != null && !nombre.isBlank()
                ? nombre.trim()
                : (originalName != null ? originalName.replaceAll("(?i)\\.pdf$", "") : "documento");

        request.setNombre(nombreDocumento);
        request.setOrden(orden);
        request.setActivo(activo);

        try {
            PDFDocumentoResponse response = pdfDocumentoService.create(
                    request, file.getBytes(), originalName);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "orden", required = false) Integer orden,
            @RequestParam(value = "activo", required = false) Boolean activo) throws IOException {

        PDFDocumentoRequest request = new PDFDocumentoRequest();
        request.setNombre(nombre);
        request.setOrden(orden);
        request.setActivo(activo);

        byte[] fileBytes = file != null && !file.isEmpty() ? file.getBytes() : null;
        String originalFilename = file != null && !file.isEmpty() ? file.getOriginalFilename() : null;

        try {
            PDFDocumentoResponse response = pdfDocumentoService.update(id, request, fileBytes, originalFilename);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pdfDocumentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reordenar")
    public ResponseEntity<List<PDFDocumentoResponse>> reordenar(@RequestBody List<Long> idsEnOrden) {
        return ResponseEntity.ok(pdfDocumentoService.reordenar(idsEnOrden));
    }
}
