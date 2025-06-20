package com.Nguyen.blogplatform.seed;

import com.Nguyen.blogplatform.Utils.SlugUtil;
import com.Nguyen.blogplatform.model.Tags;
import com.Nguyen.blogplatform.repository.TagRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class TagDataSeeder {

    @Autowired
    private TagRepository tagRepository;



    @PostConstruct
    public void seedTags() {
        if (tagRepository.count() > 0) return; // tránh seed lại nếu đã có dữ liệu

        List<Tags> tags = Arrays.asList(
                createTag("Java", "Ngôn ngữ hướng đối tượng mạnh mẽ", "#f89820"),
                createTag("JavaScript", "Ngôn ngữ web phổ biến", "#f0db4f"),
                createTag("Python", "Ngôn ngữ dễ học, dùng nhiều cho AI/ML", "#306998"),
                createTag("C++", "Ngôn ngữ hệ thống hiệu năng cao", "#00599C"),
                createTag("Go", "Ngôn ngữ hiệu quả từ Google", "#00ADD8"),
                createTag("Rust", "Ngôn ngữ an toàn bộ nhớ", "#dea584"),
                createTag("TypeScript", "JavaScript có kiểu tĩnh", "#3178c6"),
                createTag("Kotlin", "Ngôn ngữ hiện đại trên JVM", "#7F52FF"),
                createTag("Ruby", "Ngôn ngữ với cú pháp thân thiện", "#cc342d"),
                createTag("PHP", "Ngôn ngữ phổ biến cho web backend", "#8892be")
        );

        tagRepository.saveAll(tags);
    }

    private Tags createTag(String name, String description, String color) {
        String slug = SlugUtil.createSlug(name);
        Tags tag = new Tags();
        tag.setName(name);
        tag.setSlug(slug);
        tag.setDescription(description);
        tag.setColor(color);
        return tag;
    }
}
