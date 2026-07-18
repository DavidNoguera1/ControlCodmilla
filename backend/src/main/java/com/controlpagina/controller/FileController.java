package com.controlpagina.controller;

import com.controlpagina.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/api/archivos")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tipo", required = false) String tipo) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Archivo vacío"));
        }

        try {
            String filename = fileStorageService.store(file.getBytes(), file.getOriginalFilename(), tipo);
            String url = "/archivos/" + filename;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "file", Map.of("url", url, "name", filename)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false, "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/imagenesNoticias/{filename:.+}")
    public ResponseEntity<Resource> downloadNoticiaImage(@PathVariable String filename) {
        return descargar("imagenesNoticias/" + filename);
    }

    @GetMapping("/archivosPDF/{filename:.+}")
    public ResponseEntity<Resource> downloadPDF(@PathVariable String filename) {
        return descargar("archivosPDF/" + filename);
    }

    @GetMapping("/imagenesCarrusel/{filename:.+}")
    public ResponseEntity<Resource> downloadCarruselImage(@PathVariable String filename) {
        return descargar("imagenesCarrusel/" + filename);
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        return descargar(filename);
    }

    private ResponseEntity<Resource> descargar(String ruta) {
        Resource resource = fileStorageService.loadAsResource(ruta);
        String contentType = "application/octet-stream";

        try {
            contentType = Files.probeContentType(
                    java.nio.file.Paths.get(ruta)
            );
        } catch (Exception ignored) {
        }

        String nombreArchivo = ruta.contains("/") ? ruta.substring(ruta.lastIndexOf("/") + 1) : ruta;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo + "\"")
                .body(resource);
    }
}
