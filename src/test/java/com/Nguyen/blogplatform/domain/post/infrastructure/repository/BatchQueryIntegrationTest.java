package com.Nguyen.blogplatform.domain.post.infrastructure.repository;

import com.Nguyen.blogplatform.domain.comment.infrastructure.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional(readOnly = true)
class BatchQueryIntegrationTest {
    @Autowired PostRepository posts;
    @Autowired CommentRepository comments;

    @Test void batchQueriesExecuteAgainstConfiguredDatabase() {
        var ids = List.of("__nonexistent_batch_test_id__");
        assertTrue(posts.findResponseStats(ids, null).isEmpty());
        posts.findAuthorTraffic(ids.get(0));
        assertTrue(posts.findAuthorMonthlyViews(ids.get(0), 2026).isEmpty());
        assertTrue(posts.findAuthorYearlyViews(ids.get(0), 2020, 2026).isEmpty());
        assertTrue(comments.findLatestReplyIds(ids, 5).isEmpty());
        assertTrue(comments.findLatestTopLevelIds(ids, 10).isEmpty());
    }
}
