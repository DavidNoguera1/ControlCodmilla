package com.controlpagina.controller;

import com.controlpagina.dto.CarruselRequest;
import com.controlpagina.dto.CarruselResponse;
import com.controlpagina.service.CarruselService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carrusel")
public class CarruselController {

    private final CarruselService carruselService;

    public CarruselController(CarruselService carruselService) {
        this.carruselService = carruselService;
    }

    @GetMapping
    public ResponseEntity<List<CarruselResponse>> findAll() {
        return ResponseEntity.ok(carruselService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarruselResponse> findById(@PathVariable Long id) {
        return carruselService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "linkUrl", required = false) String linkUrl,
            @RequestParam(value = "activo", required = false) Boolean activo) throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacío"));
        }

        CarruselRequest request = new CarruselRequest();
        request.setTitulo(titulo);
        request.setLinkUrl(linkUrl);
        request.setActivo(activo);

        try {
            CarruselResponse response = carruselService.create(
                    request, file.getBytes(), file.getOriginalFilename());
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "linkUrl", required = false) String linkUrl,
            @RequestParam(value = "activo", required = false) Boolean activo) throws IOException {

        CarruselRequest request = new CarruselRequest();
        request.setTitulo(titulo);
        request.setLinkUrl(linkUrl);
        request.setActivo(activo);

        byte[] fileBytes = file != null && !file.isEmpty() ? file.getBytes() : null;
        String originalFilename = file != null && !file.isEmpty() ? file.getOriginalFilename() : null;

        try {
            CarruselResponse response = carruselService.update(id, request, fileBytes, originalFilename);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carruselService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reordenar")
    public ResponseEntity<List<CarruselResponse>> reordenar(@RequestBody List<Long> idsEnOrden) {
        return ResponseEntity.ok(carruselService.reordenar(idsEnOrden));
    }
}
