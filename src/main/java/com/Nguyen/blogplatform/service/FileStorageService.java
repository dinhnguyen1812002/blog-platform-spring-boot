package com.Nguyen.blogplatform.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class FileStorageService {
    private static final Logger logger = Logger.getLogger(FileStorageService.class.getName());
    private static final String THUMBNAIL_DIR = "uploads/thumbnail/";
    
    public FileStorageService() {
        // Tạo thư mục nếu chưa tồn tại
        new File(THUMBNAIL_DIR).mkdirs();
    }
    
    /**
     * Lưu file thumbnail và trả về đường dẫn
     */
    public String saveThumbnail(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        // Tạo tên file duy nhất
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(THUMBNAIL_DIR, filename);
        
        // Lưu file
        Files.write(filePath, file.getBytes());
        logger.info("Saved thumbnail: " + filePath);
        
        return filePath.toString();
    }
    
    /**
     * Xóa file thumbnail
     */
    public void deleteThumbnail(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile() && filePath.startsWith(THUMBNAIL_DIR)) {
                file.delete();
                logger.info("Deleted thumbnail: " + filePath);
            }
        } catch (Exception e) {
            logger.warning("Failed to delete thumbnail: " + filePath + ", error: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật file thumbnail (xóa cũ, lưu mới)
     */
    public String updateThumbnail(String oldFilePath, MultipartFile newFile) throws IOException {
        // Xóa file cũ nếu có
        deleteThumbnail(oldFilePath);
        
        // Lưu file mới nếu có
        if (newFile != null && !newFile.isEmpty()) {
            return saveThumbnail(newFile);
        }
        
        return oldFilePath;
    }
}