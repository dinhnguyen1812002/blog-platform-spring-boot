package com.Nguyen.blogplatform.controller.Statistics;

import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.response.TopUserResponse;

import com.Nguyen.blogplatform.service.UserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class UserRankingController {


    private final UserServices userService;

    @GetMapping("/top-authors")
    public ResponseEntity<List<TopUserResponse>> getTopAuthors(
            @RequestParam(defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(userService.getTopUser(limit));
    }
}
