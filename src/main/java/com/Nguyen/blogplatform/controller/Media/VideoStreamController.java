package com.Nguyen.blogplatform.controller.Media;

import com.Nguyen.blogplatform.model.Video;
import com.Nguyen.blogplatform.service.VideoService;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/video")
public class VideoStreamController {
    private final VideoService videoService;

    public VideoStreamController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<List<Video>> upload(@RequestParam("file") MultipartFile[] file) throws IOException {
        List<Video> videoList = videoService.uploadVideos(file);
        return new ResponseEntity<>(videoList, HttpStatus.OK);
    }
    public ResponseEntity<List<Video> >getAllVideos() {
        return new ResponseEntity<>(videoService.getAllVideos(), HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable String id) {
        videoService.deleteVideoById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stream/{id}")
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable String id,
            @RequestHeader(value = "Range", required = false) String rangeHeader
    ) throws IOException {
        Video video = videoService.getVideoById(id);
        File file = new File(video.getFilePath());
        if(!file.exists()){
            return ResponseEntity.notFound().build();
        }
        FileUrlResource videoResource = new FileUrlResource(file.getAbsolutePath());
        long fileSize = file.length();
        if(rangeHeader == null){
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(video.getContentType()))
                    .body(new ResourceRegion(videoResource, 0, fileSize));
        }
        String[] ranges= rangeHeader.replace("bytes=", "").split("-");
        long rangeStart = Long.parseLong(ranges[0]);
        long rangeEnd = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileSize-1;
        long rangeLenght = Math.min(rangeEnd - rangeStart + 1, 1_000_000L);
        ResourceRegion region = new ResourceRegion(videoResource, rangeStart, rangeEnd);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType(video.getContentType()))
                .header("Content-Range", "bytes" + rangeStart + "-"+ (rangeStart+ rangeLenght - 1 )+ "/"+ fileSize)
                .body(region);
    }
}
