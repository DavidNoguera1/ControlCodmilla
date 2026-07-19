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

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    @jakarta.annotation.PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads: " + uploadPath, e);
        }
    }

    public String store(byte[] bytes, String originalFilename) {
        return store(bytes, originalFilename, null);
    }

    public String store(byte[] bytes, String originalFilename, String subDir) {
        try {
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = java.util.UUID.randomUUID().toString() + extension;

            Path targetDir = subDir != null && !subDir.isBlank()
                    ? uploadPath.resolve(subDir).normalize()
                    : uploadPath;
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(filename).normalize();
            Files.write(targetPath, bytes);

            return subDir != null && !subDir.isBlank() ? subDir + "/" + filename : filename;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el archivo: " + originalFilename, e);
        }
    }

    public String storeWithName(byte[] bytes, String desiredName, String originalFilename, String subDir) {
        try {
            String extension = extractExtension(originalFilename);
            if (extension.isBlank()) {
                extension = extractExtension(desiredName);
            }

            String baseName = sanitizeBaseName(stripExtension(
                    desiredName != null && !desiredName.isBlank() ? desiredName : originalFilename
            ));

            Path targetDir = resolveTargetDir(subDir);
            Files.createDirectories(targetDir);

            String filename = uniqueFilename(targetDir, baseName, extension, null);
            Path targetPath = targetDir.resolve(filename).normalize();
            ensureInside(targetPath, targetDir);
            Files.write(targetPath, bytes);

            return subDir != null && !subDir.isBlank() ? subDir + "/" + filename : filename;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el archivo: " + originalFilename, e);
        }
    }

    public String rename(String currentFilename, String desiredName) {
        try {
            if (currentFilename == null || currentFilename.isBlank() || desiredName == null || desiredName.isBlank()) {
                return currentFilename;
            }

            Path currentPath = uploadPath.resolve(currentFilename).normalize();
            ensureInside(currentPath, uploadPath);

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
        } catch (Exception e) {
            throw new RuntimeException("No se pudo renombrar el archivo: " + currentFilename, e);
        }
    }

    public void delete(String filename) {
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo eliminar el archivo: " + filename, e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException("No se pudo leer el archivo: " + filename);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al leer el archivo: " + filename, e);
        }
    }

    private Path resolveTargetDir(String subDir) {
        Path targetDir = subDir != null && !subDir.isBlank()
                ? uploadPath.resolve(subDir).normalize()
                : uploadPath;
        ensureInside(targetDir, uploadPath);
        return targetDir;
    }

    private void ensureInside(Path path, Path parent) {
        if (!path.normalize().startsWith(parent.normalize())) {
            throw new RuntimeException("Ruta de archivo no permitida: " + path);
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
        String safe = normalized
                .replace('\\', '/');
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
