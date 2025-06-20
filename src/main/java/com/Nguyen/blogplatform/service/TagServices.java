package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.model.Tags;
import com.Nguyen.blogplatform.payload.request.TagRequest;
import com.Nguyen.blogplatform.payload.response.TagResponse;
import com.Nguyen.blogplatform.repository.TagRepository;
import com.github.f4b6a3.ulid.util.UlidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TagServices {
    @Autowired
    private TagRepository tagRepository;

    public void saveTag(TagRequest tagRequest) {
        boolean exists = tagRepository.existsByNameOrSlug(tagRequest.getName(), tagRequest.getSlug());
        if (exists) {
            throw new IllegalArgumentException("Tag already exists");
        }
        Tags tag = new Tags();
        tag.setName(tagRequest.getName());
        tag.setSlug(tagRequest.getSlug());
        tag.setDescription(tagRequest.getDescription());
        tag.setColor(tagRequest.getColor());
        tagRepository.save(tag);
    }

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagResponse(tag.getUuid(), tag.getName(), tag.getSlug(), tag.getDescription(), tag.getColor()))
                .collect(Collectors.toList());
    }
    public Tags getTagById(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
    }
    public void updateTag(UUID id, TagRequest tagRequest) {
        boolean exists = tagRepository.existsByNameOrSlug(tagRequest.getName(), tagRequest.getSlug());
        if (exists) {
            throw new IllegalArgumentException("Tag already exists");
        }
        Tags tag = getTagById(id);
        tag.setName(tagRequest.getName());
        tag.setSlug(tagRequest.getSlug());
        tag.setDescription(tagRequest.getDescription());
        tag.setColor(tagRequest.getColor());
        tagRepository.save(tag);
    }

//    public List<TagResponse> getTagsBySlug(String slug){
//
//    }

    public void deleteTag(UUID id) {
        tagRepository.deleteById(id);
    }

    public List<TagResponse> getLatestTags() {
        return tagRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(tag -> new TagResponse(tag.getUuid(), tag.getName(), tag.getSlug(), tag.getDescription(), tag.getColor()))
                .collect(Collectors.toList());
    }

}
