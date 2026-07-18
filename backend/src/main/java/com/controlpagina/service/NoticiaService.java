package com.controlpagina.service;

import com.controlpagina.dto.NoticiaRequest;
import com.controlpagina.dto.NoticiaResponse;
import com.controlpagina.entity.Noticia;
import com.controlpagina.repository.NoticiaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoticiaService {

    private final NoticiaRepository repository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    public NoticiaService(NoticiaRepository repository, FileStorageService fileStorageService, ObjectMapper objectMapper) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    public List<NoticiaResponse> findAll() {
        return repository.findAllByOrderByFechaPublicacionDesc()
                .stream()
                .map(NoticiaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<NoticiaResponse> findById(Long id) {
        return repository.findById(id).map(NoticiaResponse::fromEntity);
    }

    public Optional<NoticiaResponse> findBySlug(String slug) {
        return repository.findBySlug(slug).map(NoticiaResponse::fromEntity);
    }

    @Transactional
    public NoticiaResponse create(NoticiaRequest request) {
        Noticia noticia = new Noticia();
        noticia.setTitulo(request.getTitulo());
        noticia.setSlug(generarSlugUnico(request.getSlug() != null ? request.getSlug() : request.getTitulo()));
        noticia.setContenido(request.getContenido());
        noticia.setImagenPortada(request.getImagenPortada());
        noticia.setActivo(request.getActivo() != null ? request.getActivo() : true);
        return NoticiaResponse.fromEntity(repository.save(noticia));
    }

    @Transactional
    public NoticiaResponse update(Long id, NoticiaRequest request) {
        Noticia noticia = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Noticia no encontrada con id: " + id));

        String oldContenido = noticia.getContenido();
        String newContenido = request.getContenido();
        String oldPortada = noticia.getImagenPortada();
        String newPortada = request.getImagenPortada();

        noticia.setTitulo(request.getTitulo());
        noticia.setSlug(generarSlugUnico(request.getSlug() != null ? request.getSlug() : request.getTitulo(), id));
        noticia.setContenido(newContenido);
        noticia.setImagenPortada(newPortada);
        if (request.getActivo() != null) {
            noticia.setActivo(request.getActivo());
        }

        limpiarArchivosHuérfanos(oldContenido, newContenido);
        limpiarPortadaHuérfana(oldPortada, newPortada);

        return NoticiaResponse.fromEntity(repository.save(noticia));
    }

    @Transactional
    public void delete(Long id) {
        Noticia noticia = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Noticia no encontrada con id: " + id));

        eliminarArchivosDeContenido(noticia.getContenido());
        eliminarPortada(noticia.getImagenPortada());
        repository.delete(noticia);
    }

    @Transactional
    public void deleteBySlug(String slug) {
        Noticia noticia = repository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Noticia no encontrada: " + slug));
        eliminarArchivosDeContenido(noticia.getContenido());
        eliminarPortada(noticia.getImagenPortada());
        repository.delete(noticia);
    }

    private void limpiarArchivosHuérfanos(String oldContenido, String newContenido) {
        Set<String> oldFiles = extraerArchivos(oldContenido);
        Set<String> newFiles = extraerArchivos(newContenido);

        oldFiles.removeAll(newFiles);

        for (String filename : oldFiles) {
            try {
                fileStorageService.delete(filename);
            } catch (Exception ignored) {
            }
        }
    }

    private void eliminarArchivosDeContenido(String contenido) {
        Set<String> files = extraerArchivos(contenido);
        for (String filename : files) {
            try {
                fileStorageService.delete(filename);
            } catch (Exception ignored) {
            }
        }
    }

    private Set<String> extraerArchivos(String contenido) {
        Set<String> files = new HashSet<>();
        if (contenido == null || contenido.isBlank()) return files;

        try {
            JsonNode root = objectMapper.readTree(contenido);
            JsonNode blocks = root.get("blocks");
            if (blocks == null) return files;

            for (JsonNode block : blocks) {
                JsonNode data = block.get("data");
                if (data == null) continue;

                String type = block.has("type") ? block.get("type").asText() : "";

                switch (type) {
                    case "image" -> {
                        if (data.has("file")) {
                            JsonNode file = data.get("file");
                            if (file.has("url")) {
                                extraerFilename(file.get("url").asText()).ifPresent(files::add);
                            }
                        }
                    }
                    case "attaches" -> {
                        if (data.has("file")) {
                            JsonNode file = data.get("file");
                            if (file.has("url")) {
                                extraerFilename(file.get("url").asText()).ifPresent(files::add);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return files;
    }

    private void limpiarPortadaHuérfana(String oldPortada, String newPortada) {
        if (oldPortada == null || oldPortada.equals(newPortada)) return;
        extraerFilename(oldPortada).ifPresent(f -> {
            try { fileStorageService.delete(f); } catch (Exception ignored) {}
        });
    }

    private void eliminarPortada(String portada) {
        if (portada == null) return;
        extraerFilename(portada).ifPresent(f -> {
            try { fileStorageService.delete(f); } catch (Exception ignored) {}
        });
    }

    private String generarSlug(String texto) {
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalized
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String generarSlugUnico(String baseSlug) {
        return generarSlugUnico(baseSlug, null);
    }

    private String generarSlugUnico(String baseSlug, Long excludeId) {
        String slug = generarSlug(baseSlug);
        String candidate = slug;
        int counter = 1;
        while (true) {
            Optional<Noticia> existing = repository.findBySlug(candidate);
            if (existing.isEmpty() || (excludeId != null && existing.get().getId().equals(excludeId))) {
                return candidate;
            }
            candidate = slug + "-" + counter;
            counter++;
        }
    }

    private Optional<String> extraerFilename(String url) {
        if (url == null || url.isBlank()) return Optional.empty();
        if (url.contains("/archivos/")) {
            String filename = url.substring(url.lastIndexOf("/") + 1);
            if (!filename.isBlank()) return Optional.of(filename);
        }
        return Optional.empty();
    }
}
