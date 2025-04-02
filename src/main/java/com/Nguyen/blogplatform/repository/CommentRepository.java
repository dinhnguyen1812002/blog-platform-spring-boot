package com.Nguyen.blogplatform.repository;


import com.Nguyen.blogplatform.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    // Tìm tất cả replies của một comment với phân trang
    Page<Comment> findByParentCommentIdOrderByCreatedAtDesc(String parentId, Pageable pageable);

    // Tìm số lượng replies của một comment
    Long countByParentCommentId(String parentId);

    // Tìm tất cả comments cấp cao nhất (không có parent) của một post với phân trang
    Page<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtDesc(String postId, Pageable pageable);

    // Tìm tất cả comments của một post
    List<Comment> findByPostIdOrderByCreatedAtDesc(String postId);

    // Tìm tất cả comments của một user
    List<Comment> findByUserIdOrderByCreatedAtDesc(String userId);

    // Đếm số lượng comments cấp cao nhất của một post
    Long countByPostIdAndParentCommentIsNull(String postId);

    // Tìm các comments gần đây nhất của một post
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsByPostId(@Param("postId") String postId, Pageable pageable);

    // Tìm tất cả replies của một comment theo độ sâu
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.depth <= :maxDepth " +
            "ORDER BY c.createdAt DESC")
    Page<Comment> findRepliesByParentIdAndMaxDepth(
            @Param("parentId") String parentId,
            @Param("maxDepth") Integer maxDepth,
            Pageable pageable
    );

    Page<Comment> findByPostIdAndParentCommentIsNull(String postId, PageRequest createdAt);

    Page<Comment> findByParentCommentId(String commentId, PageRequest createdAt);
}