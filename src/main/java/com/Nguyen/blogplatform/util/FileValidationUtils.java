package com.Nguyen.blogplatform.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class FileValidationUtils {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");

    public static boolean isSafeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 1. Check extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return false;
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return false;
        }

        // 2. Check MIME type from Content-Type header
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            return false;
        }

        // 3. Check Magic Bytes (Basic implementation without external library)
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4) return false;

            return isImageHeader(header);
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isImageHeader(byte[] header) {
        // JPEG: FF D8 FF
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return true;
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
            return true;
        }
        // GIF: 47 49 46 38
        if (header[0] == (byte) 0x47 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x38) {
            return true;
        }
        return false;
    }

    public static String sanitizeFilename(String filename) {
        if (filename == null) return null;
        // Remove path traversal attempts and keep only alphanumeric, dots, underscores and hyphens
        String cleanName = new java.io.File(filename).getName();
        return cleanName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
