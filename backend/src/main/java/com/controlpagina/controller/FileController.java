package com.controlpagina.controller;

import com.controlpagina.service.FileStorageService;
import com.controlpagina.service.FileValidationService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/archivos")
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileValidationService fileValidationService;

    public FileController(FileStorageService fileStorageService, FileValidationService fileValidationService) {
        this.fileStorageService = fileStorageService;
        this.fileValidationService = fileValidationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tipo") String tipo) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Archivo vacío"));
        }

        try {
            String filename = fileStorageService.store(file.getBytes(), file.getOriginalFilename(), tipo);
            String url = "/archivos/" + filename;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "file", Map.of("url", url, "name", filename)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false, "error", "No se pudo guardar el archivo"
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

    @GetMapping("/imagenesTrabajadores/{filename:.+}")
    public ResponseEntity<Resource> downloadTrabajadorImage(@PathVariable String filename) {
        return descargar("imagenesTrabajadores/" + filename);
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        return descargar(filename);
    }

    private ResponseEntity<Resource> descargar(String ruta) {
        Resource resource = fileStorageService.loadAsResource(ruta);
        String contentType = fileValidationService.safeContentType(ruta);
        String nombreArchivo = ruta.contains("/") ? ruta.substring(ruta.lastIndexOf("/") + 1) : ruta;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo.replace("\"", "") + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}
