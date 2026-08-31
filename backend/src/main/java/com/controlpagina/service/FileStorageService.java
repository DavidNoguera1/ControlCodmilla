package com.controlpagina.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final FileValidationService validationService;
    private Path uploadPath;

    public FileStorageService(FileValidationService validationService) {
        this.validationService = validationService;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            for (String sub : FileValidationService.ALLOWED_SUBDIRS) {
                Files.createDirectories(uploadPath.resolve(sub));
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads: " + uploadPath, e);
        }
    }

    public String store(byte[] bytes, String originalFilename, String subDir) {
        String safeSubDir = validationService.normalizeSubDir(subDir);
        validationService.validate(bytes, originalFilename, safeSubDir);

        try {
            String extension = extractExtension(originalFilename).toLowerCase();
            String filename = UUID.randomUUID() + extension;

            Path targetDir = resolveTargetDir(safeSubDir);
            Path targetPath = targetDir.resolve(filename).normalize();
            ensureInside(targetPath, targetDir);
            Files.write(targetPath, bytes);

            return safeSubDir + "/" + filename;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el archivo", e);
        }
    }

    public String storeWithName(byte[] bytes, String desiredName, String originalFilename, String subDir) {
        String safeSubDir = validationService.normalizeSubDir(subDir);
        validationService.validate(bytes, originalFilename, safeSubDir);

        try {
            String extension = extractExtension(originalFilename);
            if (extension.isBlank()) {
                extension = extractExtension(desiredName);
            }
            extension = extension.toLowerCase();

            String baseName = sanitizeBaseName(stripExtension(
                    desiredName != null && !desiredName.isBlank() ? desiredName : originalFilename
            ));

            Path targetDir = resolveTargetDir(safeSubDir);
            String filename = uniqueFilename(targetDir, baseName, extension, null);
            Path targetPath = targetDir.resolve(filename).normalize();
            ensureInside(targetPath, targetDir);
            Files.write(targetPath, bytes);

            return safeSubDir + "/" + filename;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el archivo", e);
        }
    }

    public String rename(String currentFilename, String desiredName) {
        try {
            if (currentFilename == null || currentFilename.isBlank() || desiredName == null || desiredName.isBlank()) {
                return currentFilename;
            }

            Path currentPath = resolveExisting(currentFilename);

            if (!Files.exists(currentPath)) {
                throw new RuntimeException("No se pudo encontrar el archivo: " + currentFilename);
            }

            String extension = extractExtension(currentPath.getFileName().toString());
            String baseName = sanitizeBaseName(stripExtension(desiredName));
            Path targetDir = currentPath.getParent() != null ? currentPath.getParent() : uploadPath;
            ensureInside(targetDir, uploadPath);

            String filename = uniqueFilename(targetDir, baseName, extension, currentPath);
            Path targetPath = targetDir.resolve(filename).normalize();
            ensureInside(targetPath, targetDir);

            if (currentPath.equals(targetPath)) {
                return currentFilename;
            }

            Files.move(currentPath, targetPath);
            Path relative = uploadPath.relativize(targetPath);
            return relative.toString().replace('\\', '/');
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo renombrar el archivo", e);
        }
    }

    public void delete(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path filePath = resolveExisting(filename);
            Files.deleteIfExists(filePath);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo eliminar el archivo", e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            Path filePath = resolveExisting(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("No se pudo leer el archivo: " + filename);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al leer el archivo", e);
        }
    }

    private Path resolveExisting(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Nombre de archivo vacío");
        }
        String cleaned = filename.replace('\\', '/').trim();
        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.contains("..")) {
            throw new IllegalArgumentException("Ruta de archivo no permitida");
        }

        Path filePath = uploadPath.resolve(cleaned).normalize();
        ensureInside(filePath, uploadPath);
        return filePath;
    }

    private Path resolveTargetDir(String subDir) {
        Path targetDir = uploadPath.resolve(subDir).normalize();
        ensureInside(targetDir, uploadPath);
        return targetDir;
    }

    private void ensureInside(Path path, Path parent) {
        if (!path.normalize().startsWith(parent.normalize())) {
            throw new IllegalArgumentException("Ruta de archivo no permitida");
        }
    }

    private String uniqueFilename(Path targetDir, String baseName, String extension, Path currentPath) {
        String safeExtension = extension == null ? "" : extension;
        String candidate = baseName + safeExtension;
        int counter = 1;

        while (true) {
            Path candidatePath = targetDir.resolve(candidate).normalize();
            ensureInside(candidatePath, targetDir);
            if (!Files.exists(candidatePath) || (currentPath != null && candidatePath.equals(currentPath))) {
                return candidate;
            }
            candidate = baseName + "-" + counter + safeExtension;
            counter++;
        }
    }

    private String sanitizeBaseName(String value) {
        if (value == null || value.isBlank()) {
            return "archivo";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        String safe = normalized.replace('\\', '/');
        if (safe.contains("/")) {
            safe = safe.substring(safe.lastIndexOf("/") + 1);
        }
        safe = safe
                .replaceAll("[^A-Za-z0-9._ -]", "")
                .replaceAll("\\s+", " ")
                .replaceAll("^[. ]+|[. ]+$", "")
                .trim();

        return safe.isBlank() ? "archivo" : safe;
    }

    private String stripExtension(String filename) {
        if (filename == null) {
            return "";
        }
        String name = filename.replace('\\', '/');
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        int dot = name.lastIndexOf(".");
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        String name = filename.replace('\\', '/');
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        int dot = name.lastIndexOf(".");
        return dot >= 0 && dot < name.length() - 1 ? name.substring(dot) : "";
    }
}
