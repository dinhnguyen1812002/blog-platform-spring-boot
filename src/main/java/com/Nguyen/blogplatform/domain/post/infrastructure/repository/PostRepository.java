package com.Nguyen.blogplatform.domain.post.infrastructure.repository;



import org.springframework.data.jpa.repository.Modifying;
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
    @Query("SELECT p FROM Post p WHERE p.author = :author ORDER BY SIZE(p.likes) DESC, p.id ASC")
    List<Post> findMostLikedByAuthor(@Param("author") User author, Pageable pageable);

    @Query("SELECT COUNT(p), COALESCE(SUM(p.viewCount), 0), COALESCE(SUM(SIZE(p.likes)), 0), COALESCE(SUM(SIZE(p.comments)), 0) FROM Post p WHERE p.author.id = :userId")
    Object[] findAuthorTraffic(@Param("userId") String userId);

    @Query("SELECT MONTH(p.createdAt), COALESCE(SUM(p.viewCount), 0) FROM Post p WHERE p.author.id = :userId AND YEAR(p.createdAt) = :year GROUP BY MONTH(p.createdAt)")
    List<Object[]> findAuthorMonthlyViews(@Param("userId") String userId, @Param("year") int year);

    @Query("SELECT YEAR(p.createdAt), COALESCE(SUM(p.viewCount), 0) FROM Post p WHERE p.author.id = :userId AND YEAR(p.createdAt) BETWEEN :startYear AND :endYear GROUP BY YEAR(p.createdAt)")
    List<Object[]> findAuthorYearlyViews(@Param("userId") String userId, @Param("startYear") int startYear, @Param("endYear") int endYear);

    @Query("SELECT p.id, SIZE(p.likes), (SELECT AVG(r.score) FROM Rating r WHERE r.post = p), " +
           "(SELECT COUNT(c) FROM Comment c WHERE c.post = p), " +
           "(SELECT COUNT(l) FROM Post x JOIN x.likes l WHERE x = p AND l.id = :userId), " +
           "(SELECT MAX(r.score) FROM Rating r WHERE r.post = p AND r.user.id = :userId) " +
           "FROM Post p WHERE p.id IN :ids")
    List<Object[]> findResponseStats(@Param("ids") List<String> ids, @Param("userId") String userId);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.categories WHERE p.id IN :ids")
    List<Post> fetchAuthorsAndCategories(@Param("ids") List<String> ids);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id IN :ids")
    List<Post> fetchTags(@Param("ids") List<String> ids);


    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    Page<Post> findByVisibilityAndPublishedAtBeforeOrderByPublishedAtDesc(
            PublishStatus visibility, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = { "author" })
    Page<Post> findByFeaturedTrueAndVisibilityAndPublishedAtBeforeOrderByPublishedAtDesc(
            PublishStatus visibility, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = { "author" })
    Page<Post> findByAuthor(User author, Pageable pageable);

    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    List<Post> findByAuthor(User author);

    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    List<Post> findTop5ByAuthorOrderByCreatedAtDesc(User author);

    @EntityGraph(attributePaths = { "author", "categories", "tags" })
    List<Post> findTop5ByAuthorAndFeaturedTrueOrderByCreatedAtDesc(User author);

    @EntityGraph(attributePaths = { "author" })
    @Query("SELECT p FROM Post p WHERE p.author = :author AND p.featured = true AND p.visibility = :visibility AND (p.publishedAt IS NULL OR p.publishedAt <= :now) ORDER BY p.createdAt DESC")
    List<Post> findPublicFeaturedByAuthor(@Param("author") User author, @Param("visibility") PublishStatus visibility,
                                          @Param("now") LocalDateTime now, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + :increment WHERE p.id = :postId")
    void updateViewCount(@Param("postId") String postId, @Param("increment") Long increment);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") String postId);


    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, String id);

    // ── Likes: operate on the join table directly instead of loading Post.likes ──
    @Query(value = "SELECT COUNT(*) FROM post_like WHERE post_id = :postId AND user_id = :userId", nativeQuery = true)
    long countLike(@Param("postId") String postId, @Param("userId") String userId);

    @Query(value = "SELECT COUNT(*) FROM post_like WHERE user_id = :userId", nativeQuery = true)
    long countLikesByUserId(@Param("userId") String userId);

    @Modifying
    @Query(value = "INSERT IGNORE INTO post_like (post_id, user_id) VALUES (:postId, :userId)", nativeQuery = true)
    int insertLike(@Param("postId") String postId, @Param("userId") String userId);

    @Modifying
    @Query(value = "DELETE FROM post_like WHERE post_id = :postId AND user_id = :userId", nativeQuery = true)
    int deleteLike(@Param("postId") String postId, @Param("userId") String userId);

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
    @EntityGraph(attributePaths = { "author" })
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
