package com.Nguyen.blogplatform.domain.post.application;



import com.Nguyen.blogplatform.domain.analytics.application.ViewCountService;
import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.dto.PostSummaryResponse;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.BookmarkRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.RatingRepository;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import com.Nguyen.blogplatform.shared.mapper.PostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceRelatedPostsTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private PostMapper postMapper;
    @Mock
    private ViewCountService viewCountService;

    @InjectMocks
    private PostService postService;

    private String currentPostId = "current-post-id";

    @BeforeEach
    void setUp() {
    }

    @Test
    void getRelatedPosts_ShouldReturnPostsFromSameSeriesFirst() {
        // Arrange
        int limit = 5;
        Post seriesPost1 = Post.builder().id("series-1").build();
        Post seriesPost2 = Post.builder().id("series-2").build();
        List<Post> seriesPosts = List.of(seriesPost1, seriesPost2);

        when(postRepository.findRelatedBySeries(eq(currentPostId), any(LocalDateTime.class)))
                .thenReturn(seriesPosts);
        when(postRepository.findRelatedByCategories(eq(currentPostId), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(postRepository.findRelatedByTags(eq(currentPostId), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        
        when(postMapper.toPostSummaryList(anyList(), any(), any()))
                .thenAnswer(invocation -> {
                    List<Post> posts = invocation.getArgument(0);
                    List<PostSummaryResponse> responses = new ArrayList<>();
                    for (Post p : posts) {
                        responses.add(mock(PostSummaryResponse.class));
                    }
                    return responses;
                });

        // Act
        List<PostSummaryResponse> result = postService.getRelatedPosts(currentPostId, limit);

        // Assert
        assertEquals(2, result.size());
        verify(postRepository).findRelatedBySeries(eq(currentPostId), any());
        verify(postRepository).findRelatedByCategories(eq(currentPostId), any());
        verify(postRepository).findRelatedByTags(eq(currentPostId), any());
    }

    @Test
    void getRelatedPosts_ShouldCascadeThroughPrioritiesAndDeduplicate() {
        // Arrange
        int limit = 3;
        Post seriesPost = Post.builder().id("p1").build();
        Post categoryPost = Post.builder().id("p2").build();
        Post tagPost = Post.builder().id("p3").build();
        Post duplicatePost = Post.builder().id("p1").build(); // Same ID as seriesPost

        when(postRepository.findRelatedBySeries(eq(currentPostId), any(LocalDateTime.class)))
                .thenReturn(List.of(seriesPost));
        when(postRepository.findRelatedByCategories(eq(currentPostId), any(LocalDateTime.class)))
                .thenReturn(List.of(categoryPost, duplicatePost));
        when(postRepository.findRelatedByTags(eq(currentPostId), any(LocalDateTime.class)))
                .thenReturn(List.of(tagPost));

        when(postMapper.toPostSummaryList(anyList(), any(), any()))
                .thenAnswer(invocation -> {
                    List<Post> posts = invocation.getArgument(0);
                    assertEquals(3, posts.size()); // p1, p2, p3
                    return List.of(mock(PostSummaryResponse.class), mock(PostSummaryResponse.class), mock(PostSummaryResponse.class));
                });

        // Act
        List<PostSummaryResponse> result = postService.getRelatedPosts(currentPostId, limit);

        // Assert
        assertEquals(3, result.size());
    }

    @Test
    void getRelatedPosts_ShouldRespectLimit() {
        // Arrange
        int limit = 2;
        Post p1 = Post.builder().id("p1").build();
        Post p2 = Post.builder().id("p2").build();
        Post p3 = Post.builder().id("p3").build();

        when(postRepository.findRelatedBySeries(eq(currentPostId), any(LocalDateTime.class)))
                .thenReturn(List.of(p1, p2, p3));

        when(postMapper.toPostSummaryList(anyList(), any(), any()))
                .thenAnswer(invocation -> {
                    List<Post> posts = invocation.getArgument(0);
                    assertEquals(2, posts.size()); // Limited to 2
                    return List.of(mock(PostSummaryResponse.class), mock(PostSummaryResponse.class));
                });

        // Act
        List<PostSummaryResponse> result = postService.getRelatedPosts(currentPostId, limit);

        // Assert
        assertEquals(2, result.size());
    }
}
