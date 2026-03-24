package com.Nguyen.blogplatform.service.search;

import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.payload.response.GlobalSearchResponse;
import com.Nguyen.blogplatform.payload.response.SearchItemDTO;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.SeriesRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.repository.specification.PostSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlobalSearchService {

    private final PostRepository postRepository;
    private final SeriesRepository seriesRepository;
    private final UserRepository userRepository;

    public GlobalSearchResponse search(String keyword) {
        Pageable limitFive = PageRequest.of(0, 5);

        // Search Posts
        Specification<Post> postSpec = PostSpecification.isPublished()
                .and(PostSpecification.hasKeyword(keyword));
        List<SearchItemDTO> posts = postRepository.findAll(postSpec, limitFive)
                .getContent()
                .stream()
                .map(post -> SearchItemDTO.builder()
                        .title(post.getTitle())
                        .slug(post.getSlug())
                        .thumbnail(post.getThumbnail())
                        .authorName(post.getAuthor() != null ? post.getAuthor().getUsername() : null)
                        .view(post.getViewCount())
                        .like(post.getLikes() != null ? (long) post.getLikes().size() : 0L)
                        .build())
                .collect(Collectors.toList());

        // Search Series
        List<SearchItemDTO> series = seriesRepository.searchByKeyword(keyword, limitFive)
                .getContent()
                .stream()
                .map(s -> SearchItemDTO.builder()
                        .title(s.getTitle())
                        .slug(s.getSlug())
                        .thumbnail(s.getThumbnail())
                        .authorName(s.getUser() != null ? s.getUser().getUsername() : null)
                        .view(s.getViewCount())
                        .like(0L) // Series might not have likes in current model, setting 0
                        .build())
                .collect(Collectors.toList());

        // Search Users
        List<SearchItemDTO> users = userRepository.findAll().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(keyword.toLowerCase()))
                .limit(5)
                .map(u -> SearchItemDTO.builder()
                        .title(u.getUsername())
                        .slug(u.getSlug())
                        .thumbnail(u.getAvatar())
                        .authorName(u.getUsername())
                        .view(0L) // Users don't have views/likes in the same sense as posts
                        .like(0L)
                        .build())
                .collect(Collectors.toList());

        return GlobalSearchResponse.builder()
                .posts(posts)
                .series(series)
                .users(users)
                .build();
    }
}
