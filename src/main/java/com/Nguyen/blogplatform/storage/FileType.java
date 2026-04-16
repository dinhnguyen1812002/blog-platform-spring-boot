package com.Nguyen.blogplatform.storage;

import lombok.Getter;

import java.util.List;
import java.util.Set;

/**
 * Enum defining different file types and their validation rules.
 * Each type has specific allowed extensions, MIME types, and size limits.
 */
@Getter
public enum FileType {
    
    /**
     * User avatar images.
     * Small size limit, restricted to common image formats.
     */
    AVATAR(
            Set.of("jpg", "jpeg", "png", "gif", "webp"),
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp"),
            5L * 1024 * 1024, // 5MB
            "uploads/avatars"
    ),
    
    /**
     * Post thumbnail images.
     * Slightly larger size limit for better quality.
     */
    THUMBNAIL(
            Set.of("jpg", "jpeg", "png", "gif", "webp"),
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp"),
            10L * 1024 * 1024, // 10MB
            "uploads/thumbnails"
    ),
    
    /**
     * Post attachments and media files.
     * Larger size limit for documents and other files.
     */
    POST_ATTACHMENT(
            Set.of("jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "txt", "md"),
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp", 
                   "application/pdf", 
                   "application/msword",
                   "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                   "text/plain",
                   "text/markdown"),
            20L * 1024 * 1024, // 20MB
            "uploads/attachments"
    ),
    
    /**
     * General document uploads.
     * Most permissive for document types.
     */
    DOCUMENT(
            Set.of("pdf", "doc", "docx", "txt", "md", "rtf"),
            Set.of("application/pdf",
                   "application/msword",
                   "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                   "text/plain",
                   "text/markdown",
                   "application/rtf"),
            50L * 1024 * 1024, // 50MB
            "uploads/documents"
    );
    
    /**
     * Allowed file extensions (lowercase).
     */
    private final Set<String> allowedExtensions;
    
    /**
     * Allowed MIME types.
     */
    private final Set<String> allowedMimeTypes;
    
    /**
     * Maximum file size in bytes.
     */
    private final long maxSizeBytes;
    
    /**
     * Storage directory path (relative to base upload directory).
     */
    private final String storagePath;
    
    FileType(Set<String> allowedExtensions, 
             Set<String> allowedMimeTypes, 
             long maxSizeBytes, 
             String storagePath) {
        this.allowedExtensions = allowedExtensions;
        this.allowedMimeTypes = allowedMimeTypes;
        this.maxSizeBytes = maxSizeBytes;
        this.storagePath = storagePath;
    }
    
    /**
     * Check if a file extension is allowed for this type.
     *
     * @param extension File extension (without dot)
     * @return true if extension is allowed
     */
    public boolean isExtensionAllowed(String extension) {
        return extension != null && allowedExtensions.contains(extension.toLowerCase());
    }
    
    /**
     * Check if a MIME type is allowed for this type.
     *
     * @param mimeType MIME type
     * @return true if MIME type is allowed
     */
    public boolean isMimeTypeAllowed(String mimeType) {
        return mimeType != null && allowedMimeTypes.contains(mimeType.toLowerCase());
    }
    
    /**
     * Get allowed extensions as a comma-separated string for error messages.
     *
     * @return Comma-separated list of allowed extensions
     */
    public String getAllowedExtensionsString() {
        return String.join(", ", allowedExtensions);
    }
}
