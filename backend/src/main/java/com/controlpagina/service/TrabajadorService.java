package com.controlpagina.service;

import com.controlpagina.dto.TrabajadorPublicoResponse;
import com.controlpagina.dto.TrabajadorRequest;
import com.controlpagina.dto.TrabajadorResponse;
import com.controlpagina.entity.Trabajador;
import com.controlpagina.repository.TrabajadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TrabajadorService {

    private final TrabajadorRepository repository;
    private final FileStorageService fileStorageService;

    public TrabajadorService(TrabajadorRepository repository, FileStorageService fileStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    public List<TrabajadorResponse> findAll() {
        return repository.findAllByOrderByPrimerApellidoAscPrimerNombreAsc()
                .stream()
                .map(TrabajadorResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Consulta pública via QR.
     * Devuelve únicamente los trabajadores activos, mapeados al DTO restringido
     * que no expone IDs internos ni timestamps.
     */
    public List<TrabajadorPublicoResponse> findAllActivos() {
        return repository.findByActivoTrueOrderByPrimerApellidoAscPrimerNombreAsc()
                .stream()
                .map(TrabajadorPublicoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<TrabajadorResponse> findById(Long id) {
        return repository.findById(id).map(TrabajadorResponse::fromEntity);
    }

    public Optional<TrabajadorResponse> findByCodigoPublico(String codigoPublico) {
        return repository.findByCodigoPublico(normalizeCode(codigoPublico)).map(TrabajadorResponse::fromEntity);
    }

    @Transactional
    public TrabajadorResponse create(TrabajadorRequest request, byte[] fileBytes, String originalFilename) {
        validateRequired(request.getPrimerNombre(), "El primer nombre es obligatorio");
        validateRequired(request.getPrimerApellido(), "El primer apellido es obligatorio");

        String codigoPublico = resolveNewCode(request.getCodigoPublico());
        String fotoUrl = fileStorageService.store(fileBytes, originalFilename, "imagenesTrabajadores");

        Trabajador trabajador = new Trabajador();
        trabajador.setCodigoPublico(codigoPublico);
        trabajador.setPrimerNombre(cleanRequired(request.getPrimerNombre()));
        trabajador.setSegundoNombre(cleanOptional(request.getSegundoNombre()));
        trabajador.setPrimerApellido(cleanRequired(request.getPrimerApellido()));
        trabajador.setSegundoApellido(cleanOptional(request.getSegundoApellido()));
        trabajador.setFotoUrl(fotoUrl);
        trabajador.setActivo(request.getActivo() != null ? request.getActivo() : true);

        return TrabajadorResponse.fromEntity(repository.save(trabajador));
    }

    @Transactional
    public TrabajadorResponse update(Long id, TrabajadorRequest request, byte[] fileBytes, String originalFilename) {
        Trabajador trabajador = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado con id: " + id));

        if (request.getCodigoPublico() != null) {
            String codigoPublico = normalizeCode(request.getCodigoPublico());
            validateRequired(codigoPublico, "El codigo publico es obligatorio");
            if (repository.existsByCodigoPublicoAndIdNot(codigoPublico, id)) {
                throw new RuntimeException("Ya existe un trabajador con el codigo publico: " + codigoPublico);
            }
            trabajador.setCodigoPublico(codigoPublico);
        }
        if (request.getPrimerNombre() != null) {
            trabajador.setPrimerNombre(cleanRequired(request.getPrimerNombre()));
        }
        if (request.getSegundoNombre() != null) {
            trabajador.setSegundoNombre(cleanOptional(request.getSegundoNombre()));
        }
        if (request.getPrimerApellido() != null) {
            trabajador.setPrimerApellido(cleanRequired(request.getPrimerApellido()));
        }
        if (request.getSegundoApellido() != null) {
            trabajador.setSegundoApellido(cleanOptional(request.getSegundoApellido()));
        }
        if (request.getActivo() != null) {
            trabajador.setActivo(request.getActivo());
        }
        if (fileBytes != null) {
            fileStorageService.delete(trabajador.getFotoUrl());
            String fotoUrl = fileStorageService.store(fileBytes, originalFilename, "imagenesTrabajadores");
            trabajador.setFotoUrl(fotoUrl);
        }

        return TrabajadorResponse.fromEntity(repository.save(trabajador));
    }

    @Transactional
    public void delete(Long id) {
        Trabajador trabajador = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabajador no encontrado con id: " + id));

        fileStorageService.delete(trabajador.getFotoUrl());
        repository.delete(trabajador);
    }

    private String resolveNewCode(String requestedCode) {
        String normalized = normalizeCode(requestedCode);
        if (normalized != null && !normalized.isBlank()) {
            if (repository.existsByCodigoPublico(normalized)) {
                throw new RuntimeException("Ya existe un trabajador con el codigo publico: " + normalized);
            }
            return normalized;
        }

        String generated;
        do {
            generated = "tr-" + UUID.randomUUID().toString().substring(0, 8);
        } while (repository.existsByCodigoPublico(generated));
        return generated;
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return null;
        }
        String code = value.trim();
        if (code.startsWith("#")) {
            code = code.substring(1);
        }
        return code;
    }

    private String cleanRequired(String value) {
        validateRequired(value, "Campo obligatorio");
        return value.trim();
    }

    private String cleanOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }
    }
}
