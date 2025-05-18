package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Utils.UrlUtils;
import com.Nguyen.blogplatform.model.Video;
import com.Nguyen.blogplatform.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final Logger logger = Logger.getLogger(VideoService.class.getName());

    public VideoService(VideoRepository videoRepository, @Value("${video.storage.path}") String storagePath) {
        this.videoRepository = videoRepository;
        this.storagePath = storagePath;
        new File(storagePath).mkdirs();
    }

    public List<Video> uploadVideos(MultipartFile[] files) throws IOException {
        List<Video> savedVideos = new ArrayList<>();
        UrlUtils urlUtils = new UrlUtils();
        for (MultipartFile fileItem : files) {
            String filename = System.currentTimeMillis() + "_" + fileItem.getOriginalFilename();
            Path path = Paths.get(storagePath, filename);
            Files.write(path, fileItem.getBytes());
            String url =  urlUtils.getBaseEnvLinkURL() + path.toString()  ;
            Video video = new Video();
            video.setTitle(fileItem.getOriginalFilename());
            video.setFilePath(path.toString());
            video.setUrl(url);
            video.setContentType(fileItem.getContentType());
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
