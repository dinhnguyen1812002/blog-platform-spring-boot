package com.Nguyen.blogplatform.shared.mapper;

import com.Nguyen.blogplatform.domain.comment.application.CommentService;
import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.BookmarkRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostMapperBatchTest {
    @Test void summaryUsesBatchStatisticsAndBookmarks() {
        PostRepository posts = mock(PostRepository.class);
        BookmarkRepository bookmarks = mock(BookmarkRepository.class);
        PostMapper mapper = new PostMapper(mock(CommentService.class), posts);
        Post one = Post.builder().id("1").title("one").build();
        Post two = Post.builder().id("2").title("two").build();
        User user = new User(); user.setId("user");
        when(posts.findResponseStats(List.of("1", "2"), "user")).thenReturn(List.of(
                new Object[]{"1", 3, 4.0, 2L, 1L, 4},
                new Object[]{"2", 0, null, 0L, 0L, null}));
        when(bookmarks.findBookmarkedPostIds(eq(user), anyCollection())).thenReturn(Set.of("2"));

        var result = mapper.toPostSummaryList(List.of(one, two), user, bookmarks);
        assertEquals(3L, result.get(0).likeCount());
        assertTrue(result.get(0).isLikedByCurrentUser());
        assertFalse(result.get(0).isSavedByCurrentUser());
        assertEquals(0.0, result.get(1).averageRating());
        assertTrue(result.get(1).isSavedByCurrentUser());
        verify(posts, times(1)).findResponseStats(anyList(), eq("user"));
        verify(bookmarks, times(1)).findBookmarkedPostIds(eq(user), anyCollection());
    }
}
