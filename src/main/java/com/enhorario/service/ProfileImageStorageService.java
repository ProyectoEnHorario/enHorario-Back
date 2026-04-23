package com.enhorario.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfileImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final Path uploadDir;

    public ProfileImageStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String storeProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("El archivo debe ser una imagen valida");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("La imagen no puede superar 5 MB");
        }

        try {
            Files.createDirectories(uploadDir);

            String extension = resolveExtension(file.getOriginalFilename(), file.getContentType());
            String fileName = "profile_" + UUID.randomUUID() + extension;
            Path targetPath = uploadDir.resolve(fileName).normalize();

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/api/v1/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen de perfil", e);
        }
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            String lowerName = originalFilename.toLowerCase();
            if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
                return ".jpg";
            }
            if (lowerName.endsWith(".png")) {
                return ".png";
            }
            if (lowerName.endsWith(".webp")) {
                return ".webp";
            }
            if (lowerName.endsWith(".gif")) {
                return ".gif";
            }
        }

        if ("image/png".equals(contentType)) {
            return ".png";
        }
        if ("image/webp".equals(contentType)) {
            return ".webp";
        }
        if ("image/gif".equals(contentType)) {
            return ".gif";
        }

        return ".jpg";
    }
}