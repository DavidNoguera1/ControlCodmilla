package com.controlpagina.service;

import com.controlpagina.dto.PDFDocumentoRequest;
import com.controlpagina.dto.PDFDocumentoResponse;
import com.controlpagina.entity.PDFDocumento;
import com.controlpagina.repository.PDFDocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PDFDocumentoService {

    private final PDFDocumentoRepository repository;
    private final FileStorageService fileStorageService;

    public PDFDocumentoService(PDFDocumentoRepository repository, FileStorageService fileStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    public List<PDFDocumentoResponse> findAll() {
        return repository.findAllByOrderByOrdenAsc()
                .stream()
                .map(PDFDocumentoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<PDFDocumentoResponse> findById(Long id) {
        return repository.findById(id).map(PDFDocumentoResponse::fromEntity);
    }

    @Transactional
    public PDFDocumentoResponse create(PDFDocumentoRequest request, byte[] fileBytes, String originalFilename) {
        int maxOrden = repository.findMaxOrden();

        String rutaArchivo = fileStorageService.store(fileBytes, originalFilename, "archivosPDF");

        PDFDocumento doc = new PDFDocumento();
        doc.setNombre(request.getNombre());
        doc.setNombreOriginal(originalFilename);
        doc.setRutaArchivo(rutaArchivo);
        doc.setOrden(request.getOrden() != null ? request.getOrden() : maxOrden + 1);
        doc.setActivo(request.getActivo() != null ? request.getActivo() : true);

        return PDFDocumentoResponse.fromEntity(repository.save(doc));
    }

    @Transactional
    public PDFDocumentoResponse update(Long id, PDFDocumentoRequest request, byte[] fileBytes, String originalFilename) {
        PDFDocumento doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PDF no encontrado con id: " + id));

        if (fileBytes != null) {
            fileStorageService.delete(doc.getRutaArchivo());
            String rutaArchivo = fileStorageService.store(fileBytes, originalFilename, "archivosPDF");
            doc.setRutaArchivo(rutaArchivo);
            doc.setNombreOriginal(originalFilename);
        }

        if (request.getNombre() != null) {
            doc.setNombre(request.getNombre());
        }
        if (request.getOrden() != null) {
            doc.setOrden(request.getOrden());
        }
        if (request.getActivo() != null) {
            doc.setActivo(request.getActivo());
        }

        return PDFDocumentoResponse.fromEntity(repository.save(doc));
    }

    @Transactional
    public void delete(Long id) {
        PDFDocumento doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PDF no encontrado con id: " + id));

        fileStorageService.delete(doc.getRutaArchivo());
        repository.delete(doc);
    }

    @Transactional
    public List<PDFDocumentoResponse> reordenar(List<Long> idsEnOrden) {
        List<PDFDocumento> docs = repository.findAllByOrderByOrdenAsc();

        Map<Long, PDFDocumento> docMap = docs.stream()
                .collect(Collectors.toMap(PDFDocumento::getId, d -> d));

        for (int i = 0; i < idsEnOrden.size(); i++) {
            PDFDocumento doc = docMap.get(idsEnOrden.get(i));
            if (doc != null) {
                doc.setOrden(i + 1);
            }
        }

        repository.saveAll(docs);

        return repository.findAllByOrderByOrdenAsc()
                .stream()
                .map(PDFDocumentoResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
