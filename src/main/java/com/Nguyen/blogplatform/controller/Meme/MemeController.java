package com.Nguyen.blogplatform.controller.Meme;

import com.Nguyen.blogplatform.model.Meme;
import com.Nguyen.blogplatform.payload.request.MemeRequest;
import com.Nguyen.blogplatform.payload.response.PagedMemeResponse;
import com.Nguyen.blogplatform.service.MemeServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/v1/memes")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MemeController {
    private static final Logger logger = Logger.getLogger(MemeController.class.getName());
    @Autowired
    private MemeServices memeService;

    @PostMapping("/upload")
    public ResponseEntity<Meme> uploadMeme(
            @RequestParam("meme") String memeJson,
            @RequestParam("file") MultipartFile file) throws Exception {

        // Chuyển JSON string thành object
        ObjectMapper objectMapper = new ObjectMapper();
        MemeRequest memeRequest = objectMapper.readValue(memeJson, MemeRequest.class);

        Meme meme = memeService.addMeme(memeRequest, file);
        return ResponseEntity.ok(meme);
    }


    @PostMapping("/upload/multiple")
    public ResponseEntity<List<Meme>>  UploadMemeMultiple(
            @RequestParam("memes") List<MemeRequest> memeRequest,
            @RequestPart("file") List<MultipartFile> file
    )throws Exception{
        try {
            List<Meme>memes = memeService.addMultipleMemes(memeRequest, file);
            return ResponseEntity.ok(memes);
        }catch (Exception e) {

            logger.warning(e.getMessage());
            return ResponseEntity.badRequest().build();
        }

    }

    @GetMapping
    public ResponseEntity<PagedMemeResponse> getAllMemes(
            @RequestParam(defaultValue = "0") int page) {
        Page<Meme> memes = memeService.getAllMemes(page);
        return ResponseEntity.ok(new PagedMemeResponse(memes));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Meme> getMemeBySlug(@PathVariable String slug) {
        try {
            Meme meme = memeService.getMemeBySlug(slug);
            return ResponseEntity.ok(meme);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/random-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRandomMeme() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Giữ kết nối mở mãi mãi
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            try {
                Meme randomMeme = memeService.getRandomMeme();
                if (randomMeme != null) {
                    emitter.send(SseEmitter.event()
                            .name("random-meme")
                            .data(randomMeme));
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, 0, 5, TimeUnit.MINUTES); // Gửi meme mới mỗi 5 phút

        // Đóng executor khi emitter hoàn thành
        emitter.onCompletion(executor::shutdown);
        emitter.onError((e) -> executor.shutdown());

        return emitter;
    }

}
