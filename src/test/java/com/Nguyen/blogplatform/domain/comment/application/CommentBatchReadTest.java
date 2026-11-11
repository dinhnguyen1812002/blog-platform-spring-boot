package com.Nguyen.blogplatform.domain.comment.application;

import com.Nguyen.blogplatform.domain.comment.domain.model.Comment;
import com.Nguyen.blogplatform.domain.comment.infrastructure.repository.CommentRepository;
import com.Nguyen.blogplatform.domain.notification.application.NotificationService;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CommentBatchReadTest {
    @Test void buildsNestedRepliesInBatchesAndPreservesOrder() {
        var repository = mock(CommentRepository.class);
        var service = new CommentService(repository, mock(PostRepository.class),
                mock(UserRepository.class), mock(CommentNotificationDispatcher.class));
        Comment root = comment("root", 0, 6, 1);
        Comment first = comment("first", 1, 1, 3);
        Comment second = comment("second", 1, 0, 2);
        Comment nested = comment("nested", 2, 0, 4);
        first.setParentComment(root);
        second.setParentComment(root);
        nested.setParentComment(first);
        when(repository.findByPostIdAndParentCommentIsNull(eq("post"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(root)));
        when(repository.findLatestReplyIds(List.of("root"), 5)).thenReturn(List.of("first", "second"));
        when(repository.findByIdIn(List.of("first", "second"))).thenReturn(List.of(second, first));
        when(repository.findLatestReplyIds(List.of("first"), 5)).thenReturn(List.of("nested"));
        when(repository.findByIdIn(List.of("nested"))).thenReturn(List.of(nested));

        var result = service.getTopLevelComments("post", PageRequest.of(0, 10)).getContent().get(0);
        assertTrue(result.isHasMoreReplies());
        assertEquals(List.of("first", "second"), result.getReplies().stream().map(c -> c.getId()).toList());
        assertEquals("nested", result.getReplies().get(0).getReplies().get(0).getId());
        verify(repository, times(2)).findLatestReplyIds(anyList(), eq(5));
    }

    private Comment comment(String id, int depth, int replies, long time) {
        Comment c = new Comment();
        c.setId(id);
        c.setDepth(depth);
        c.setReplyCount(replies);
        c.setCreatedAt(Instant.ofEpochSecond(time));
        return c;
    }
}
