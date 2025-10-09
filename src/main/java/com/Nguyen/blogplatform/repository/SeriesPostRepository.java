package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.SeriesPost;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeriesPostRepository extends JpaRepository<SeriesPost, Long> {

//    List<SeriesPost> findBySeriesIdOrderByPositionAsc(String seriesId);

    Optional<SeriesPost> findBySeriesIdAndPostId(String seriesId, String postId);

    @Query("select coalesce(max(sp.orderIndex), 0) from SeriesPost sp where sp.series.id = :seriesId")
//    Integer findMaxPositionBySeriesId(@Param("seriesId") String seriesId);

//    void deleteBySeriesIdAndPostId(String seriesId, String postId);

    boolean existsBySeriesIdAndPostId(String seriesId, @NotEmpty(message = "Post ID is required") String postId);

    @Query("SELECT MAX(sp.orderIndex) FROM SeriesPost sp WHERE sp.series.id = :seriesId")
    Optional<Integer> findMaxOrderIndexBySeriesId(@Param("seriesId") String seriesId);

    List<SeriesPost> findBySeriesIdAndOrderIndexGreaterThan(String seriesId, int i);

    Number countBySeriesId(String seriesId);

    List<SeriesPost> findBySeriesIdOrderByOrderIndexAsc(String seriesId);
}
