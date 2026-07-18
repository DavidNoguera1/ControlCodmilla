package com.controlpagina.controller;

import com.controlpagina.dto.NoticiaRequest;
import com.controlpagina.dto.NoticiaResponse;
import com.controlpagina.service.NoticiaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {

    private final NoticiaService noticiaService;

    public NoticiaController(NoticiaService noticiaService) {
        this.noticiaService = noticiaService;
    }

    @GetMapping
    public ResponseEntity<List<NoticiaResponse>> findAll() {
        return ResponseEntity.ok(noticiaService.findAll());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<NoticiaResponse> findBySlug(@PathVariable String slug) {
        return noticiaService.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NoticiaResponse> create(@Valid @RequestBody NoticiaRequest request) {
        return ResponseEntity.status(201).body(noticiaService.create(request));
    }

    @PutMapping("/{slug}")
    public ResponseEntity<NoticiaResponse> update(@PathVariable String slug, @Valid @RequestBody NoticiaRequest request) {
        return noticiaService.findBySlug(slug)
                .map(existing -> ResponseEntity.ok(noticiaService.update(existing.getId(), request)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> delete(@PathVariable String slug) {
        noticiaService.deleteBySlug(slug);
        return ResponseEntity.noContent().build();
    }
}
