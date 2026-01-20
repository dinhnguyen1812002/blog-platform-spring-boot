package com.Nguyen.blogplatform.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
public class LocalAvatarStorageService implements AvatarStorageService {

    private final Path uploadRoot;
    private final long maxSizeBytes;
    private final Set<String> allowedExtensions = Set.of("png", "jpg", "jpeg", "gif", "webp");

    public LocalAvatarStorageService(
            @Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDir,
            @Value("${app.upload.max-size-bytes:5242880}") long maxSizeBytes
    ) {
        this.uploadRoot = Paths.get(avatarDir).toAbsolutePath().normalize();
        this.maxSizeBytes = maxSizeBytes;
        try {
            Files.createDirectories(this.uploadRoot);

        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File too large. Max size: " + maxSizeBytes + " bytes");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar");
        String ext = getExtension(original);
        if (!allowedExtensions.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: ." + ext);
        }

        String filename = "avatar-" + Instant.now().toEpochMilli() + "." + ext;
        Path userDir = uploadRoot.resolve(userId);
        try {
            Files.createDirectories(userDir);
            Path target = userDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Upload root = {}", uploadRoot);
            log.info("Exists = {}", Files.exists(uploadRoot));
            log.info("Writable = {}", Files.isWritable(uploadRoot));
            // Return stored relative path from uploads root: /uploads/avatars/{userId}/{filename}
            return "/uploads/avatars/" + userId + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store avatar for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to store avatar", e);
        }
    }

    @Override
    public void delete(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) return;
        try {
            String prefix = "/uploads/";
            String relative = publicUrl.startsWith(prefix) ? publicUrl.substring(prefix.length()) : publicUrl;
            Path path = Paths.get("uploads").resolve(relative).normalize().toAbsolutePath();
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
            log.warn("Failed to delete old avatar {}: {}", publicUrl, e.getMessage());
        }
    }

    @Override
    public String getPublicUrl(String storedPath) {
        // For local storage we already return a public URL under /uploads/**
        return storedPath;
    }

    private String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return (i > 0 && i < filename.length() - 1) ? filename.substring(i + 1) : "";
    }
}
