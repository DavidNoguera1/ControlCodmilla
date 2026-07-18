package com.controlpagina.service;

import com.controlpagina.dto.CarruselRequest;
import com.controlpagina.dto.CarruselResponse;
import com.controlpagina.entity.Carrusel;
import com.controlpagina.repository.CarruselRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarruselService {

    private final CarruselRepository repository;
    private final FileStorageService fileStorageService;

    public CarruselService(CarruselRepository repository, FileStorageService fileStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    public List<CarruselResponse> findAll() {
        return repository.findAllByOrderByOrdenAsc()
                .stream()
                .map(CarruselResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<CarruselResponse> findById(Long id) {
        return repository.findById(id).map(CarruselResponse::fromEntity);
    }

    @Transactional
    public CarruselResponse create(CarruselRequest request, byte[] fileBytes, String originalFilename) {
        int maxOrden = repository.findMaxOrden();

        String rutaImagen = fileStorageService.store(fileBytes, originalFilename, "imagenesCarrusel");

        Carrusel c = new Carrusel();
        c.setTitulo(request.getTitulo() != null ? request.getTitulo() : originalFilename);
        c.setImagenUrl(rutaImagen);
        c.setLinkUrl(request.getLinkUrl());
        c.setOrden(request.getOrden() != null ? request.getOrden() : maxOrden + 1);
        c.setActivo(request.getActivo() != null ? request.getActivo() : true);

        return CarruselResponse.fromEntity(repository.save(c));
    }

    @Transactional
    public CarruselResponse update(Long id, CarruselRequest request, byte[] fileBytes, String originalFilename) {
        Carrusel c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con id: " + id));

        if (fileBytes != null) {
            fileStorageService.delete(c.getImagenUrl());
            String rutaImagen = fileStorageService.store(fileBytes, originalFilename, "imagenesCarrusel");
            c.setImagenUrl(rutaImagen);
        }

        if (request.getTitulo() != null) {
            c.setTitulo(request.getTitulo());
        }
        if (request.getLinkUrl() != null) {
            c.setLinkUrl(request.getLinkUrl());
        }
        if (request.getOrden() != null) {
            c.setOrden(request.getOrden());
        }
        if (request.getActivo() != null) {
            c.setActivo(request.getActivo());
        }

        return CarruselResponse.fromEntity(repository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        Carrusel c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con id: " + id));

        fileStorageService.delete(c.getImagenUrl());
        repository.delete(c);
    }

    @Transactional
    public List<CarruselResponse> reordenar(List<Long> idsEnOrden) {
        List<Carrusel> items = repository.findAllByOrderByOrdenAsc();

        Map<Long, Carrusel> itemMap = items.stream()
                .collect(Collectors.toMap(Carrusel::getId, i -> i));

        for (int i = 0; i < idsEnOrden.size(); i++) {
            Carrusel item = itemMap.get(idsEnOrden.get(i));
            if (item != null) {
                item.setOrden(i + 1);
            }
        }

        repository.saveAll(items);

        return repository.findAllByOrderByOrdenAsc()
                .stream()
                .map(CarruselResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
