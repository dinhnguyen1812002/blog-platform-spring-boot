package com.Nguyen.blogplatform.controller.Series;


import com.Nguyen.blogplatform.payload.request.series.*;
import com.Nguyen.blogplatform.payload.response.ApiResponse;
import com.Nguyen.blogplatform.service.series.SeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller để quản lý Series
 */
@RestController
@RequestMapping("/api/v1/series")
@RequiredArgsConstructor
@Tag(name = "Series Management", description = "APIs for managing blog series")
public class SeriesController {

    private final SeriesService seriesService;

    /**
     * Tạo mới series
     * POST /api/series
     */
    @PostMapping
    @Operation(summary = "Create a new series", description = "Create a new series for organizing posts")
    public ResponseEntity<ApiResponse<SeriesResponseDTO>> createSeries(
            @Valid @RequestBody CreateSeriesDTO dto,
            Authentication authentication) throws BadRequestException {

        String userId = authentication.getName(); // Lấy user ID từ authentication
        SeriesResponseDTO response = seriesService.createSeries(dto, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Series created successfully", response));
    }

    /**
     * Cập nhật thông tin series
     * PUT /api/series/{seriesId}
     */
    @PutMapping("/{seriesId}")
    @Operation(summary = "Update series", description = "Update series information")
    public ResponseEntity<ApiResponse<SeriesResponseDTO>> updateSeries(
            @PathVariable String seriesId,
            @Valid @RequestBody UpdateSeriesDTO dto,
            Authentication authentication) throws BadRequestException {

        String userId = authentication.getName();
        SeriesResponseDTO response = seriesService.updateSeries(seriesId, dto, userId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Series updated successfully", response));
    }

    /**
     * Lấy chi tiết series theo ID
     * GET /api/series/{seriesId}
     */
    @GetMapping("/{seriesId}")
    @Operation(summary = "Get series by ID", description = "Retrieve series details by ID")
    public ResponseEntity<ApiResponse<SeriesResponseDTO>> getSeriesById(@PathVariable String seriesId) {
        SeriesResponseDTO response = seriesService.getSeriesById(seriesId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Series retrieved successfully", response));
    }

    /**
     * Lấy chi tiết series theo slug
     * GET /api/series/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get series by slug", description = "Retrieve series details by slug")
    public ResponseEntity<ApiResponse<SeriesResponseDTO>> getSeriesBySlug(@PathVariable String slug) {
        SeriesResponseDTO response = seriesService.getSeriesBySlug(slug);
        return ResponseEntity.ok(new ApiResponse<>(true, "Series retrieved successfully", response));
    }

    /**
     * Lấy danh sách tất cả series với phân trang
     * GET /api/series?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    @Operation(summary = "Get all series", description = "Retrieve paginated list of all series")
    public ResponseEntity<ApiResponse<Page<SeriesListDTO>>> getAllSeries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<SeriesListDTO> response = seriesService.getAllSeries(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Series list retrieved successfully", response));
    }

    /**
     * Lấy danh sách series của một tác giả
     * GET /api/series/user/{userId}?page=0&size=10
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get series by user", description = "Retrieve series created by a specific user")
    public ResponseEntity<ApiResponse<Page<SeriesListDTO>>> getSeriesByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<SeriesListDTO> response = seriesService.getSeriesByUserId(userId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "User series retrieved successfully", response));
    }

    /**
     * Tìm kiếm series
     * POST /api/series/search
     */
    @PostMapping("/search")
    @Operation(summary = "Search series", description = "Search series with various filters")
    public ResponseEntity<ApiResponse<Page<SeriesListDTO>>> searchSeries(
            @RequestBody SeriesSearchDTO searchDTO) {

        Page<SeriesListDTO> response = seriesService.searchSeries(searchDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Series search completed", response));
    }

    /**
     * Lấy các series phổ biến nhất
     * GET /api/series/popular?page=0&size=10
     */
    @GetMapping("/popular")
    @Operation(summary = "Get popular series", description = "Retrieve most viewed series")
    public ResponseEntity<ApiResponse<Page<SeriesListDTO>>> getMostViewedSeries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<SeriesListDTO> response = seriesService.getMostViewedSeries(page, size);
        return ResponseEntity.ok(new ApiResponse<>(true, "Popular series retrieved successfully", response));
    }

    /**
     * Thêm bài viết vào series
     * POST /api/series/{seriesId}/posts
     */
    @PostMapping("/{seriesId}/posts")
    @Operation(summary = "Add post to series", description = "Add a post to a series")
    public ResponseEntity<ApiResponse<SeriesResponseDTO>> addPostToSeries(
            @PathVariable String seriesId,
            @Valid @RequestBody AddPostToSeriesDTO dto,
            Authentication authentication) throws BadRequestException {

        String userId = authentication.getName();
        SeriesResponseDTO response = seriesService.addPostToSeries(seriesId, dto, userId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Post added to series successfully", response));
    }

    /**
     * Xóa bài viết khỏi series
     * DELETE /api/series/{seriesId}/posts/{postId}
     */
    @DeleteMapping("/{seriesId}/posts/{postId}")
    @Operation(summary = "Remove post from series", description = "Remove a post from a series")
    public ResponseEntity<ApiResponse<SeriesResponseDTO>> removePostFromSeries(
            @PathVariable String seriesId,
            @PathVariable String postId,
            Authentication authentication) throws BadRequestException {

        String userId = authentication.getName();
        SeriesResponseDTO response = seriesService.removePostFromSeries(seriesId, postId, userId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Post removed from series successfully", response));
    }

    /**
     * Sắp xếp lại thứ tự bài viết trong series
     * PUT /api/series/{seriesId}/posts/reorder
     */
    @PutMapping("/{seriesId}/posts/reorder")
    @Operation(summary = "Reorder posts in series", description = "Change the order of posts in a series")
    public ResponseEntity<ApiResponse<SeriesResponseDTO>> reorderPost(
            @PathVariable String seriesId,
            @Valid @RequestBody ReorderSeriesPostDTO dto,
            Authentication authentication) throws BadRequestException {

        String userId = authentication.getName();
        SeriesResponseDTO response = seriesService.reorderPost(seriesId, dto, userId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Post reordered successfully", response));
    }

    /**
     * Xóa series
     * DELETE /api/series/{seriesId}
     */
    @DeleteMapping("/{seriesId}")
    @Operation(summary = "Delete series", description = "Delete a series")
    public ResponseEntity<ApiResponse<Void>> deleteSeries(
            @PathVariable String seriesId,
            Authentication authentication) throws BadRequestException {

        String userId = authentication.getName();
        seriesService.deleteSeries(seriesId, userId);

        return ResponseEntity.ok(new ApiResponse<>(true, "Series deleted successfully", null));
    }
}

