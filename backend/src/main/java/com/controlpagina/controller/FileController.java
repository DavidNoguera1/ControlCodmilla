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
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Archivo vacío"));
        }

        try {
            String filename = fileStorageService.store(file.getBytes(), file.getOriginalFilename());
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

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        Resource resource = fileStorageService.loadAsResource(filename);
        String contentType = "application/octet-stream";

        try {
            contentType = Files.probeContentType(
                    java.nio.file.Paths.get(filename)
            );
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
