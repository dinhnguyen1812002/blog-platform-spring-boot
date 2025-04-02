package com.Nguyen.blogplatform.controller;

import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.github.f4b6a3.ulid.UlidCreator;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


@RestController
@RequestMapping("/video")
public class VideoStreamController {
//    private static final String VIDEO_UPLOAD_DIR = "uploads/videos/";
    private static final String VIDEO_UPLOAD_DIR = "./src/main/resources/videos/";
    public VideoStreamController() {
            File dir = new File(VIDEO_UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    public String generateFileName(String originalFileName) {
        String ulid = UlidCreator.getUlid().toString();
        return ulid + "_" + originalFileName;
    }
    @PostMapping("/upload")
    public ResponseEntity<MessageResponse> uploadVideo(@RequestParam("file") MultipartFile file) {
        // Kiểm tra xem tệp có rỗng hay không
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Vui lòng chọn một tệp video để tải lên! "));
        }

        // Kiểm tra định dạng tệp

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        // Generate new file name with UUID or ULID
        String newFileName = generateFileName(originalFileName);
        if (!newFileName.endsWith(".mp4") && !newFileName.endsWith(".avi") && !newFileName.endsWith(".mkv")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(new MessageResponse("Định dạng video không hỗ trợ. Chỉ hỗ trợ các định dạng: .mp4, .avi, .mkv"));
        }

        try {
            // Đường dẫn đầy đủ của tệp tải lên
            Path uploadPath = Paths.get(VIDEO_UPLOAD_DIR + newFileName);

            // Lưu tệp vào server
            Files.copy(file.getInputStream(), uploadPath);

            // Phản hồi thành công
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new MessageResponse("Tải lên thành công: " + newFileName));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Đã xảy ra lỗi khi tải lên tệp video!"));
        }
    }

}
