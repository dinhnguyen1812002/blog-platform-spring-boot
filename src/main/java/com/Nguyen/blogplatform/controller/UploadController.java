package com.Nguyen.blogplatform.controller;


import com.Nguyen.blogplatform.Utils.UrlUtils;
import com.Nguyen.blogplatform.payload.response.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
import java.util.List;
import java.util.Objects;
@RestController

@RequestMapping("/api/v1/upload")

public class UploadController {
    String IMAGE_FOLDER = "./src/main/resources/images/";
    private UrlUtils url;
    @PostMapping()
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadfile) {

        ResponseResult rr = new ResponseResult();

        if (uploadfile.isEmpty()) {
            return ResponseEntity.ok().body("please select a file!");
        }

        try {
            String[] fileUrls = saveUploadedFiles(List.of(uploadfile));
            rr.setMessage(fileUrls[0]);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }

        rr.setStatusCode(200);
        return ResponseEntity.ok(rr);
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

            Path path = Paths.get(IMAGE_FOLDER+filename);
            Files.write(path, bytes);
            fileUrls[index] =  url.getBaseEnvLinkURL() + "/images/"+filename;
            index++;
        }
        return fileUrls;
    }

//    public String getBaseEnvLinkURL() {
//        String baseEnvLinkURL=null;
//        HttpServletRequest currentRequest =
//                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//        baseEnvLinkURL = "http://" + currentRequest.getLocalName();
//        if(currentRequest.getLocalPort() != 80) {
//            baseEnvLinkURL += ":" + currentRequest.getLocalPort();
//        }
//        if(!StringUtils.isEmpty(currentRequest.getContextPath())) {
//            baseEnvLinkURL += currentRequest.getContextPath();
//        }
//        return baseEnvLinkURL;
//    }
}