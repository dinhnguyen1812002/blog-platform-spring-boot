package com.Nguyen.blogplatform.controller.Log;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/logs")
public class LogController {

    @GetMapping
    public String getLogViewer() {
        return "Log/index";
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<String> getLogContent() {
        try {
            Path path = Paths.get("logs/app.log");
            String content = Files.readString(path);
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading log file: " + e.getMessage());
        }
    }
}
