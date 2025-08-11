package com.Nguyen.blogplatform.service;


import com.Nguyen.blogplatform.Utils.UrlUtils;
import com.Nguyen.blogplatform.model.Meme;
import com.Nguyen.blogplatform.payload.request.MemeRequest;
import com.Nguyen.blogplatform.repository.MemeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class MemeServices {
    @Autowired
    private MemeRepository memeRepository;


    private final String UPLOAD_DIR = "uploads/";
    private Random random = new Random();
    private long lastRandomTime = 0;
    private Meme lastRandomMeme = null;

    // Thêm một meme với file upload
    public Meme addMeme(MemeRequest request, MultipartFile file) throws Exception {
        // Tạo thư mục upload nếu chưa tồn tại
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Lưu file và tạo URL
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        String memeUrl = UPLOAD_DIR + fileName;

        Meme meme = new Meme();
        meme.setName(request.getName());
        meme.setDescription(request.getDescription());
        meme.setMemeUrl(memeUrl);

        return memeRepository.save(meme);
    }

    public List<Meme> addMultipleMemes(List<MemeRequest> requests, List<MultipartFile> files) throws Exception {
        if (requests.size() != files.size()) {
            throw new IllegalArgumentException("Number of requests must match number of files");
        }

        List<Meme> memes = new java.util.ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            memes.add(addMeme(requests.get(i), files.get(i)));
        }
        return memes;
    }

    public Meme getRandomMeme() {
        long now = System.currentTimeMillis();
        if(now - lastRandomTime >=  300000 ||  lastRandomMeme == null)  {
            List<Meme>  allMemes =  memeRepository.findAll();
            if(allMemes.isEmpty()) return null;
            lastRandomMeme = allMemes.get(random.nextInt(allMemes.size()));
            lastRandomTime = now;
        }
        return lastRandomMeme;
    }
    public Page<Meme> getAllMemes(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Meme> memes = memeRepository.findAll(pageable);

        // Lấy base URL hiện tại
        String baseUrl = UrlUtils.getBaseEnvLinkURL();

        // Map lại URL của mỗi meme
        memes.forEach(m -> {
            if (m.getMemeUrl() != null && !m.getMemeUrl().startsWith("http")) {
                m.setMemeUrl(baseUrl + m.getMemeUrl());
            }
        });

        return memes;
    }

    
    public Meme getMemeBySlug(String slug) {
        return memeRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Meme not found with slug: " + slug));
    }

}
