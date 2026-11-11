package com.Nguyen.blogplatform.domain.comment.infrastructure.repository;




import com.Nguyen.blogplatform.domain.comment.domain.model.Comment;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    @EntityGraph(attributePaths = "user")
    Page<Comment> findByPostIdAndParentCommentIsNull(String postId, PageRequest createdAt);

    @EntityGraph(attributePaths = "user")
    Page<Comment> findByParentCommentId(String commentId, PageRequest createdAt);

    @Query(value = "SELECT ranked.id FROM (SELECT c.id, ROW_NUMBER() OVER (PARTITION BY c.parent_id ORDER BY c.created_at DESC, c.id DESC) AS rn FROM comment c WHERE c.parent_id IN (:parentIds)) ranked WHERE ranked.rn <= :limit", nativeQuery = true)
    List<String> findLatestReplyIds(@Param("parentIds") List<String> parentIds, @Param("limit") int limit);

    @Query(value = "SELECT ranked.id FROM (SELECT c.id, ROW_NUMBER() OVER (PARTITION BY c.post_id ORDER BY c.created_at DESC, c.id DESC) AS rn FROM comment c WHERE c.post_id IN (:postIds) AND c.parent_id IS NULL) ranked WHERE ranked.rn <= :limit", nativeQuery = true)
    List<String> findLatestTopLevelIds(@Param("postIds") List<String> postIds, @Param("limit") int limit);

    @EntityGraph(attributePaths = "user")
    List<Comment> findByIdIn(List<String> ids);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user = :user")
    Long countByUser(@Param("user") User user);


}