package com.Nguyen.blogplatform.controller.Tags;

import com.Nguyen.blogplatform.payload.request.TagRequest;
import com.Nguyen.blogplatform.payload.response.TagResponse;
import com.Nguyen.blogplatform.service.TagServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    @Autowired
    private TagServices tagServices;

    @PostMapping
    public ResponseEntity<String> createTag(@RequestBody TagRequest tagRequest) {
        tagServices.saveTag(tagRequest);
        return ResponseEntity.ok("Tag created successfully");
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagServices.getAllTags());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable UUID id) {
        var tag = tagServices.getTagById(id);
        var response = new TagResponse(tag.getUuid(), tag.getName(), tag.getSlug(), tag.getDescription(), tag.getColor());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTag(@PathVariable UUID id, @RequestBody TagRequest tagRequest) {
        tagServices.updateTag(id, tagRequest);
        return ResponseEntity.ok("Tag updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTag(@PathVariable UUID id) {
        tagServices.deleteTag(id);
        return ResponseEntity.ok("Tag deleted successfully");
    }

    @GetMapping("/latest")
    public ResponseEntity<List<TagResponse>> getLatestTags() {
        return ResponseEntity.ok(tagServices.getLatestTags());
    }

//    @GetMapping("/trending")
//    public ResponseEntity<List<TagResponse>> getTrendingTags() {
//        return ResponseEntity.ok(tagServices.getTrendingTags());
//    }
}
