package com.Nguyen.blogplatform.domain.media.application;

import com.Nguyen.blogplatform.shared.util.FileValidationUtils;



import com.Nguyen.blogplatform.domain.media.domain.model.Video;
import com.Nguyen.blogplatform.domain.media.infrastructure.repository.VideoRepository;
import com.Nguyen.blogplatform.shared.util.UrlUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    private final String storagePath;

    private static final java.util.Set<String> ALLOWED_VIDEO_TYPES =
            java.util.Set.of("video/mp4", "video/webm", "video/ogg");

    private final Logger logger = Logger.getLogger(VideoService.class.getName());

    public VideoService(VideoRepository videoRepository, @Value("${video.storage.path}") String storagePath) {
        this.videoRepository = videoRepository;
        this.storagePath = storagePath;
        new File(storagePath).mkdirs();
    }

    public List<Video> uploadVideos(MultipartFile[] files) throws IOException {
        List<Video> savedVideos = new ArrayList<>();
        UrlUtils urlUtils = new UrlUtils();
        Path storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
        for (MultipartFile fileItem : files) {
            String contentType = fileItem.getContentType();
            if (fileItem.isEmpty() || contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("Only MP4, WebM or Ogg video files are allowed");
            }
            String safeName = FileValidationUtils.sanitizeFilename(fileItem.getOriginalFilename());
            if (safeName == null || safeName.isBlank()) safeName = "video";
            String filename = System.currentTimeMillis() + "_" + safeName;
            Path path = storageRoot.resolve(filename).normalize();
            // Defense in depth: never write outside the storage directory
            if (!path.startsWith(storageRoot)) {
                throw new IllegalArgumentException("Invalid file name");
            }
            Files.write(path, fileItem.getBytes());
            String url =  urlUtils.getBaseEnvLinkURL() + path.toString()  ;
            Video video = new Video();
            video.setTitle(safeName);
            video.setFilePath(path.toString());
            video.setUrl(url);
            video.setContentType(contentType);
            savedVideos.add(videoRepository.save(video));
            logger.info(path.toString());
        }
        return savedVideos;
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public Video getVideoById(String id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));
    }
    public void deleteVideoById(String id) {
        Video video = getVideoById(id);
        File file = new File(video.getFilePath());
        if(file.exists()){
            file.delete();
        }
        videoRepository.delete(video);

    }

}
