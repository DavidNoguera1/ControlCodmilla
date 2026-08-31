package com.controlpagina.service;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Validación de uploads: whitelist de carpetas, extensiones y magic bytes.
 */
@Component
public class FileValidationService {

    public static final Set<String> ALLOWED_SUBDIRS = Set.of(
            "imagenesNoticias",
            "imagenesCarrusel",
            "imagenesTrabajadores",
            "archivosPDF"
    );

    private static final Map<String, Set<String>> EXT_BY_DIR = Map.of(
            "imagenesNoticias", Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif"),
            "imagenesCarrusel", Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif"),
            "imagenesTrabajadores", Set.of(".jpg", ".jpeg", ".png", ".webp"),
            "archivosPDF", Set.of(".pdf")
    );

    private static final Set<String> IMAGE_EXTS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    public String normalizeSubDir(String subDir) {
        if (subDir == null || subDir.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el tipo/carpeta de destino del archivo");
        }
        String normalized = subDir.trim().replace('\\', '/');
        if (normalized.contains("..") || normalized.contains("/") || normalized.contains("\\")) {
            throw new IllegalArgumentException("Tipo de archivo no permitido");
        }
        if (!ALLOWED_SUBDIRS.contains(normalized)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + normalized);
        }
        return normalized;
    }

    public void validate(byte[] bytes, String originalFilename, String subDir) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Archivo vacío");
        }
        if (bytes.length > 20 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo (20MB)");
        }

        String dir = normalizeSubDir(subDir);
        String ext = extractExtension(originalFilename).toLowerCase(Locale.ROOT);
        Set<String> allowed = EXT_BY_DIR.get(dir);
        if (allowed == null || !allowed.contains(ext)) {
            throw new IllegalArgumentException(
                    "Extensión no permitida para " + dir + ": " + (ext.isBlank() ? "(sin extensión)" : ext));
        }

        if (!matchesMagic(bytes, ext)) {
            throw new IllegalArgumentException(
                    "El contenido del archivo no coincide con la extensión declarada");
        }
    }

    public String safeContentType(String filename) {
        String ext = extractExtension(filename).toLowerCase(Locale.ROOT);
        return switch (ext) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            case ".gif" -> "image/gif";
            case ".pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    public boolean isImageExtension(String filename) {
        return IMAGE_EXTS.contains(extractExtension(filename).toLowerCase(Locale.ROOT));
    }

    private boolean matchesMagic(byte[] bytes, String ext) {
        if (bytes.length < 4) return false;
        return switch (ext) {
            case ".jpg", ".jpeg" ->
                    (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
            case ".png" ->
                    bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47;
            case ".gif" ->
                    bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F';
            case ".webp" ->
                    bytes.length >= 12
                            && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                            && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
            case ".pdf" ->
                    bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
            default -> false;
        };
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        String name = filename.replace('\\', '/');
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf('/') + 1);
        }
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : "";
    }
}
