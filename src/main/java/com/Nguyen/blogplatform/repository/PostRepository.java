package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.Enum.PublishStatus;
import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {

    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    Page<Post> findByVisibilityAndPublishedAtBeforeOrderByPublishedAtDesc(
            PublishStatus visibility, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    Page<Post> findByFeaturedTrueAndVisibilityAndPublishedAtBeforeOrderByPublishedAtDesc(
            PublishStatus visibility, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    Page<Post> findByAuthor(User author, Pageable pageable);

    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    List<Post> findByAuthor(User author);

    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    List<Post> findTop5ByAuthorOrderByCreatedAtDesc(User author);

    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    List<Post> findTop5ByAuthorAndFeaturedTrueOrderByCreatedAtDesc(User author);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") String postId);

    boolean existsByTitleIgnoreCase(String title);

    boolean existsBySlug(String slug);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.author = :user")
    Long countByAuthor(@Param("user") User user);

    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    Page<Post> findByAuthorUsername(String username, Pageable pageable);

    @Query("""
                SELECT DISTINCT p FROM Post p
                LEFT JOIN p.categories c
                LEFT JOIN p.tags t
                WHERE p.author.username = :username
                AND (:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                  OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
                AND (:categoryName IS NULL OR LOWER(c.category) LIKE LOWER(CONCAT('%', :categoryName, '%')))
                AND (:tagName IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :tagName, '%')))
            """)
    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    Page<Post> findByAuthorWithFilters(
            @Param("username") String username,
            @Param("keyword") String keyword,
            @Param("categoryName") String categoryName,
            @Param("tagName") String tagName,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.visibility = 'SCHEDULED' AND p.scheduledPublishAt <= :now")
    List<Post> findDueToPublish(LocalDateTime now);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Post> findByCreatedAtAfter(LocalDateTime createdAtAfter);

    <T> ScopedValue<T> findBySlug(String slug);
}
