package com.controlpagina.controller;

import com.controlpagina.dto.PDFDocumentoRequest;
import com.controlpagina.dto.PDFDocumentoResponse;
import com.controlpagina.service.PDFDocumentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    public ResponseEntity<PDFDocumentoResponse> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "orden", required = false) Integer orden,
            @RequestParam(value = "activo", required = false) Boolean activo) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(null);
        }

        String nombreDocumento = nombre != null && !nombre.isBlank()
                ? nombre.trim()
                : originalName.replaceAll("(?i)\\.pdf$", "");

        PDFDocumentoRequest request = new PDFDocumentoRequest();
        request.setNombre(nombreDocumento);
        request.setOrden(orden);
        request.setActivo(activo);

        PDFDocumentoResponse response = pdfDocumentoService.create(
                request, file.getBytes(), originalName);

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PDFDocumentoResponse> update(
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

        if (originalFilename != null && !originalFilename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(null);
        }

        PDFDocumentoResponse response = pdfDocumentoService.update(id, request, fileBytes, originalFilename);
        return ResponseEntity.ok(response);
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
