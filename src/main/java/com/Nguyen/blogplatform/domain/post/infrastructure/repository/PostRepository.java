package com.Nguyen.blogplatform.domain.post.infrastructure.repository;



import com.Nguyen.blogplatform.domain.analytics.dto.MonthlyStatDTO;
import com.Nguyen.blogplatform.domain.analytics.dto.TopPostDTO;
import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.series.domain.model.SeriesPost;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.shared.enums.PublishStatus;
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
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + :increment WHERE p.id = :postId")
    void updateViewCount(@Param("postId") String postId, @Param("increment") Long increment);

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

    Optional<Post> findBySlug(String slug);

    @EntityGraph(attributePaths = { "author" })
    Page<Post> findByAuthorAndVisibility(User author, PublishStatus visibility, Pageable pageable);

    @Query("SELECT SUM(p.viewCount) FROM Post p")
    Long sumAllViewCounts();

    @Query("SELECT SUM(SIZE(p.likes)) FROM Post p")
    Long sumAllLikes();

    @Query("SELECT NEW com.Nguyen.blogplatform.domain.analytics.dto.TopPostDTO(" +
           "p.id, p.title, u.username, p.viewCount, SIZE(p.likes)) " +
           "FROM Post p JOIN p.author u " +
           "ORDER BY p.viewCount DESC")
    List<com.Nguyen.blogplatform.domain.analytics.dto.TopPostDTO> findTopPostsByViews(Pageable pageable);

    @Query("SELECT NEW com.Nguyen.blogplatform.domain.analytics.dto.TopPostDTO(" +
           "p.id, p.title, u.username, p.viewCount, SIZE(p.likes)) " +
           "FROM Post p JOIN p.author u " +
           "ORDER BY SIZE(p.likes) DESC")
    List<com.Nguyen.blogplatform.domain.analytics.dto.TopPostDTO> findTopPostsByLikes(Pageable pageable);

    @Query("SELECT new com.Nguyen.blogplatform.domain.analytics.dto.MonthlyStatDTO(" +
           "YEAR(p.createdAt), MONTH(p.createdAt), COUNT(p)) " +
           "FROM Post p " +
           "WHERE YEAR(p.createdAt) = :year " +
           "GROUP BY YEAR(p.createdAt), MONTH(p.createdAt) " +
           "ORDER BY MONTH(p.createdAt) ASC")
    List<com.Nguyen.blogplatform.domain.analytics.dto.MonthlyStatDTO> countPostsPerMonth(@Param("year") int year);

    @Query("SELECT YEAR(p.createdAt), MONTH(p.createdAt), SUM(p.viewCount), COUNT(p.id) " +
            "FROM Post p " +
            "WHERE YEAR(p.createdAt) = :year " +
            "GROUP BY YEAR(p.createdAt), MONTH(p.createdAt) " +
            "ORDER BY MONTH(p.createdAt) ASC")
    List<Object[]> getMonthlyStatsWithViewsAndPosts(@Param("year") int year);

    @Query("""
            SELECT p FROM Post p
            WHERE p.id != :excludePostId
            AND p.visibility = 'PUBLISHED'
            AND (p.publishedAt IS NULL OR p.publishedAt <= :now)
            AND p.id IN (
                SELECT sp.post.id FROM SeriesPost sp WHERE sp.series.id IN (
                    SELECT isp.series.id FROM SeriesPost isp WHERE isp.post.id = :excludePostId
                )
            )
            ORDER BY p.publishedAt DESC
            """)
    List<Post> findRelatedBySeries(@Param("excludePostId") String excludePostId,
                                   @Param("now") LocalDateTime now);

    @Query("""
            SELECT DISTINCT p FROM Post p
            JOIN p.categories c
            WHERE p.id != :excludePostId
            AND p.visibility = 'PUBLISHED'
            AND (p.publishedAt IS NULL OR p.publishedAt <= :now)
            AND c.id IN (
                SELECT ic.id FROM Post ip JOIN ip.categories ic WHERE ip.id = :excludePostId
            )
            ORDER BY p.publishedAt DESC
            """)
    List<Post> findRelatedByCategories(@Param("excludePostId") String excludePostId,
                                       @Param("now") LocalDateTime now);

    @Query("""
            SELECT DISTINCT p FROM Post p
            JOIN p.tags t
            WHERE p.id != :excludePostId
            AND p.visibility = 'PUBLISHED'
            AND (p.publishedAt IS NULL OR p.publishedAt <= :now)
            AND t.uuid IN (
                SELECT it.uuid FROM Post ip JOIN ip.tags it WHERE ip.id = :excludePostId
            )
            ORDER BY p.publishedAt DESC
            """)
    List<Post> findRelatedByTags(@Param("excludePostId") String excludePostId,
                                 @Param("now") LocalDateTime now);
}
