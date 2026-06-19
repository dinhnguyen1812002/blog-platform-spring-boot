package com.Nguyen.blogplatform.domain.search.api;



import com.Nguyen.blogplatform.domain.search.application.GlobalSearchService;
import com.Nguyen.blogplatform.domain.search.dto.GlobalSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping
    public ResponseEntity<GlobalSearchResponse> globalSearch(@RequestParam String q) {
        return ResponseEntity.ok(globalSearchService.search(q));
    }
}
