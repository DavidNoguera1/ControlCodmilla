package com.controlpagina.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

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
}
