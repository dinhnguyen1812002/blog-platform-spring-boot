package com.Nguyen.blogplatform.domain.search.application;



import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.specification.PostSpecification;
import com.Nguyen.blogplatform.domain.search.dto.GlobalSearchResponse;
import com.Nguyen.blogplatform.domain.search.dto.SearchItemDTO;
import com.Nguyen.blogplatform.domain.series.domain.model.Series;
import com.Nguyen.blogplatform.domain.series.infrastructure.repository.SeriesRepository;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public GlobalSearchResponse search(String keyword) {
        if (keyword == null || keyword.isBlank() || keyword.length() > 200)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Search keyword must contain 1 to 200 characters");
        keyword = keyword.trim();
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
        List<SearchItemDTO> users = userRepository.findByUsernameContainingIgnoreCaseOrderByUsernameAsc(keyword, limitFive).stream()
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
