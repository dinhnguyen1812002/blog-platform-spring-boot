package com.Nguyen.blogplatform.repository;


import com.Nguyen.blogplatform.model.Series;
import com.Nguyen.blogplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository để thao tác với Series entity
 */
@Repository
public interface SeriesRepository extends JpaRepository<Series, String> {

    /**
     * Tìm series theo slug
     */
    Optional<Series> findBySlug(String slug);

    /**
     * Kiểm tra slug đã tồn tại chưa
     */
    boolean existsBySlug(String slug);

    /**
     * Lấy danh sách series của một tác giả
     */
    Page<Series> findByUser(User user, Pageable pageable);

    /**
     * Lấy danh sách series của một tác giả theo user ID
     */
    Page<Series> findByUserId(String userId, Pageable pageable);

    /**
     * Tìm series đang active
     */
    Page<Series> findByIsActiveTrue(Pageable pageable);

    /**
     * Tìm series đã hoàn thành
     */
    Page<Series> findByIsCompletedTrue(Pageable pageable);

    /**
     * Tìm series của một tác giả và active
     */
    Page<Series> findByUserIdAndIsActiveTrue(String userId, Pageable pageable);

    /**
     * Tìm kiếm series theo title hoặc description
     */
    @Query("SELECT s FROM Series s WHERE " +
            "LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Series> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Lấy series phổ biến nhất (theo view count)
     */
    @Query("SELECT s FROM Series s WHERE s.isActive = true ORDER BY s.viewCount DESC")
    Page<Series> findMostViewed(Pageable pageable);

    /**
     * Đếm số series của một user
     */
    long countByUserId(String userId);

    /**
     * Tìm các series có chứa một post cụ thể
     */
    @Query("SELECT s FROM Series s JOIN s.seriesPosts sp WHERE sp.post.id = :postId")
    List<Series> findByPostId(@Param("postId") String postId);
}