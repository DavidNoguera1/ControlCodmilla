package com.controlpagina.controller;

import com.controlpagina.dto.CarruselRequest;
import com.controlpagina.dto.CarruselResponse;
import com.controlpagina.service.CarruselService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    public ResponseEntity<CarruselResponse> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "titulo", required = false) String titulo,
            @RequestParam(value = "linkUrl", required = false) String linkUrl,
            @RequestParam(value = "activo", required = false) Boolean activo) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        CarruselRequest request = new CarruselRequest();
        request.setTitulo(titulo);
        request.setLinkUrl(linkUrl);
        request.setActivo(activo);

        CarruselResponse response = carruselService.create(
                request, file.getBytes(), file.getOriginalFilename());

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarruselResponse> update(
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

        CarruselResponse response = carruselService.update(id, request, fileBytes, originalFilename);
        return ResponseEntity.ok(response);
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
