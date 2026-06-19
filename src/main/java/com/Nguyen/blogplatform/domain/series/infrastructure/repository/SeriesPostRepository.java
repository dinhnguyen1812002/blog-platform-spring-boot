package com.Nguyen.blogplatform.domain.series.infrastructure.repository;



import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.series.domain.model.SeriesPost;
import com.Nguyen.blogplatform.domain.series.dto.SeriesPostDTO;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeriesPostRepository extends JpaRepository<SeriesPost, Long> {

//    List<SeriesPost> findBySeriesIdOrderByPositionAsc(String seriesId);

    Optional<SeriesPost> findBySeriesIdAndPostId(String seriesId, String postId);

//    void deleteBySeriesIdAndPostId(String seriesId, String postId);

    boolean existsBySeriesIdAndPostId(String seriesId, @NotEmpty(message = "Post ID is required") String postId);

    @Query("SELECT MAX(sp.orderIndex) FROM SeriesPost sp WHERE sp.series.id = :seriesId")
    Optional<Integer> findMaxOrderIndexBySeriesId(@Param("seriesId") String seriesId);

    List<SeriesPost> findBySeriesIdAndOrderIndexGreaterThan(String seriesId, int i);

    Number countBySeriesId(String seriesId);

    List<SeriesPost> findBySeriesIdOrderByOrderIndexAsc(String seriesId);

    @Query("""
            SELECT new com.Nguyen.blogplatform.domain.series.dto.SeriesPostDTO(
                p.id,
                p.title,
                p.slug,
                p.excerpt,
                p.thumbnail,
                sp.orderIndex,
                sp.createdAt,
                p.publishedAt
            )
            FROM SeriesPost sp
            JOIN sp.post p
            WHERE sp.series.id = :seriesId
            ORDER BY sp.orderIndex ASC
            """)
    List<SeriesPostDTO> findPostDtosBySeriesId(@Param("seriesId") String seriesId);
}
