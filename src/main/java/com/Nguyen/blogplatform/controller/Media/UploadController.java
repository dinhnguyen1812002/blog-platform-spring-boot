package com.Nguyen.blogplatform.controller.Media;


import com.Nguyen.blogplatform.Utils.UrlUtils;
import com.Nguyen.blogplatform.payload.response.ResponseResult;
import com.Nguyen.blogplatform.service.UserDetailsImpl;
import com.Nguyen.blogplatform.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
@RestController

@RequestMapping("/api/v1/upload")

public class UploadController {
    String IMAGE_FOLDER = "./src/main/resources/images/";
    private static final String THUMBNAIL_DIR = "uploads/thumbnail/";
    private static final String AVATAR_DIR = "uploads/avatar/";
    public static final String PUBLIC_UPLOAD_PATH = "/uploads/thumbnail/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_CONTENT_TYPES = {"image/jpeg", "image/png", "image/gif"};


    private UrlUtils url;
    private final UserProfileService userProfileService;

    public UploadController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping()
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadfile) {
        ResponseResult rr = new ResponseResult();

        if (uploadfile.isEmpty()) {
            return ResponseEntity.ok().body("please select a file!");
        }

        try {
            // Create directories if they don't exist
            Path thumbnailPath = Paths.get(THUMBNAIL_DIR);
            if (!Files.exists(thumbnailPath)) {
                Files.createDirectories(thumbnailPath);
            }
            
            String[] fileUrls = saveUploadedFiles(List.of(uploadfile));
            rr.setMessage(fileUrls[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ResponseResult(400, "Error uploading file: " + e.getMessage()));
        }

        rr.setStatusCode(200);
        Map<String, String> response = new HashMap<>();
        response.put("url", rr.getMessage());
        response.put("message", "File uploaded successfully");
        response.put("statusCode", "200");
        return ResponseEntity.ok(response);
    }

    /**
     * Upload avatar for authenticated user
     * Validates file type (JPEG, PNG, GIF), size (max 5MB), saves to uploads/avatar/, updates user profile
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "File is empty"));
            }

            // Check file size
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "File size exceeds 5MB limit"));
            }

            // Check content type
            String contentType = file.getContentType();
            boolean isValidType = false;
            for (String allowedType : ALLOWED_CONTENT_TYPES) {
                if (allowedType.equals(contentType)) {
                    isValidType = true;
                    break;
                }
            }
            if (!isValidType) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid file type. Only JPEG, PNG, and GIF are allowed"));
            }

            // Create avatar directory if not exists
            Path avatarPath = Paths.get(AVATAR_DIR);
            if (!Files.exists(avatarPath)) {
                Files.createDirectories(avatarPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = avatarPath.resolve(filename);

            // Save file
            Files.write(filePath, file.getBytes());

            // Generate URL
            String avatarUrl = getBaseEnvLinkURL() + AVATAR_DIR + filename;

            // Update user profile
            userProfileService.updateUserAvatar(userDetails.getId(), avatarUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avatar uploaded successfully");
            response.put("avatarUrl", avatarUrl);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error uploading file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Unexpected error: " + e.getMessage()));
        }
    }

    //save file
    private String[] saveUploadedFiles(List<MultipartFile> files) throws IOException {
        String[] fileUrls = new String[files.size()];
        int index = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            byte[] bytes = file.getBytes();
            long TICKS_AT_EPOCH = 621355968000000000L;
            long tick = System.currentTimeMillis()*10000 + TICKS_AT_EPOCH;
            String filename = String.valueOf(tick).concat("_")
                    .concat(Objects
                            .requireNonNull(file.getOriginalFilename()));

            Path path = Paths.get(THUMBNAIL_DIR + filename);
            Files.write(path, bytes);
            
            // Return the URL that can be used to access the file
            fileUrls[index] = getBaseEnvLinkURL() + "/uploads/thumbnail/" + filename;
            index++;
        }
        return fileUrls;
    }

    // public String getBaseEnvLinkURL() {
    //     String baseEnvLinkURL=null;
    //     if(url == null) {
    //         url = new UrlUtils();
    //     }
    //     HttpServletRequest currentRequest =
    //             ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    //     baseEnvLinkURL = "http://" + currentRequest.getLocalName();
    //     if(currentRequest.getLocalPort() != 80) {
    //         baseEnvLinkURL += ":" + currentRequest.getLocalPort();
    //     }
    //     if(!StringUtils.isEmpty(currentRequest.getContextPath())) {
    //         baseEnvLinkURL += currentRequest.getContextPath();
    //     }
    //     return baseEnvLinkURL;
    // }

    public String getBaseEnvLinkURL() {
    HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

    String url = request.getRequestURL().toString();
    String uri = request.getRequestURI();
    return url.replace(uri, request.getContextPath());
}

}