package com.controlpagina.controller;

import com.controlpagina.dto.TrabajadorRequest;
import com.controlpagina.dto.TrabajadorResponse;
import com.controlpagina.service.TrabajadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/trabajadores")
public class TrabajadorController {

    private final TrabajadorService trabajadorService;

    public TrabajadorController(TrabajadorService trabajadorService) {
        this.trabajadorService = trabajadorService;
    }

    @GetMapping
    public ResponseEntity<List<TrabajadorResponse>> findAll() {
        return ResponseEntity.ok(trabajadorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrabajadorResponse> findById(@PathVariable Long id) {
        return trabajadorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/publico/{codigoPublico}")
    public ResponseEntity<TrabajadorResponse> findByCodigoPublico(@PathVariable String codigoPublico) {
        return trabajadorService.findByCodigoPublico(codigoPublico)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TrabajadorResponse> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "codigoPublico", required = false) String codigoPublico,
            @RequestParam("primerNombre") String primerNombre,
            @RequestParam(value = "segundoNombre", required = false) String segundoNombre,
            @RequestParam("primerApellido") String primerApellido,
            @RequestParam(value = "segundoApellido", required = false) String segundoApellido,
            @RequestParam(value = "activo", required = false) Boolean activo) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        TrabajadorRequest request = buildRequest(
                codigoPublico, primerNombre, segundoNombre, primerApellido, segundoApellido, activo);

        TrabajadorResponse response = trabajadorService.create(
                request, file.getBytes(), file.getOriginalFilename());

        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrabajadorResponse> update(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "codigoPublico", required = false) String codigoPublico,
            @RequestParam(value = "primerNombre", required = false) String primerNombre,
            @RequestParam(value = "segundoNombre", required = false) String segundoNombre,
            @RequestParam(value = "primerApellido", required = false) String primerApellido,
            @RequestParam(value = "segundoApellido", required = false) String segundoApellido,
            @RequestParam(value = "activo", required = false) Boolean activo) throws IOException {

        TrabajadorRequest request = buildRequest(
                codigoPublico, primerNombre, segundoNombre, primerApellido, segundoApellido, activo);

        byte[] fileBytes = file != null && !file.isEmpty() ? file.getBytes() : null;
        String originalFilename = file != null && !file.isEmpty() ? file.getOriginalFilename() : null;

        TrabajadorResponse response = trabajadorService.update(id, request, fileBytes, originalFilename);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trabajadorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private TrabajadorRequest buildRequest(
            String codigoPublico,
            String primerNombre,
            String segundoNombre,
            String primerApellido,
            String segundoApellido,
            Boolean activo) {
        TrabajadorRequest request = new TrabajadorRequest();
        request.setCodigoPublico(codigoPublico);
        request.setPrimerNombre(primerNombre);
        request.setSegundoNombre(segundoNombre);
        request.setPrimerApellido(primerApellido);
        request.setSegundoApellido(segundoApellido);
        request.setActivo(activo);
        return request;
    }
}
