package com.Nguyen.blogplatform.storage;

import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {
    String store(MultipartFile file, String userId);
    void delete(String path);
    String getPublicUrl(String storedPath);
}
