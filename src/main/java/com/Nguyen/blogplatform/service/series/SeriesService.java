package com.Nguyen.blogplatform.service.series;

import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.*;
import com.Nguyen.blogplatform.payload.request.series.*;
import com.Nguyen.blogplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý business logic cho Series
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final SeriesPostRepository seriesPostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Tạo mới series
     */
    public SeriesResponseDTO createSeries(CreateSeriesDTO dto, String userId) throws BadRequestException {
        log.info("Creating new series: {} by user: {}", dto.getTitle(), userId);

        // Kiểm tra user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Kiểm tra slug đã tồn tại chưa
        if (seriesRepository.existsBySlug(dto.getSlug())) {
            throw new BadRequestException("Series with slug '" + dto.getSlug() + "' already exists");
        }

        // Tạo series mới
        Series series = Series.builder()
                .title(dto.getTitle())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .thumbnail(dto.getThumbnail())
                .user(user)
                .isActive(dto.getIsActive())
                .isCompleted(dto.getIsCompleted())
                .totalPosts(0)
                .viewCount(0L)
                .build();

        series = seriesRepository.save(series);
        log.info("Series created successfully with id: {}", series.getId());

        return mapToResponseDTO(series);
    }

    /**
     * Cập nhật thông tin series
     */
    public SeriesResponseDTO updateSeries(String seriesId, UpdateSeriesDTO dto, String userId) throws BadRequestException {
        log.info("Updating series: {} by user: {}", seriesId, userId);

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("Series not found with id: " + seriesId));

        // Kiểm tra quyền sở hữu
        if (!series.getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have permission to update this series");
        }

        // Cập nhật thông tin
        if (dto.getTitle() != null) {
            series.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            series.setDescription(dto.getDescription());
        }
        if (dto.getThumbnail() != null) {
            series.setThumbnail(dto.getThumbnail());
        }
        if (dto.getIsActive() != null) {
            series.setIsActive(dto.getIsActive());
        }
        if (dto.getIsCompleted() != null) {
            series.setIsCompleted(dto.getIsCompleted());
        }

        series = seriesRepository.save(series);
        log.info("Series updated successfully: {}", seriesId);

        return mapToResponseDTO(series);
    }

    /**
     * Lấy chi tiết series theo ID
     */
    @Transactional(readOnly = true)
    public SeriesResponseDTO getSeriesById(String seriesId) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("Series not found with id: " + seriesId));

        return mapToResponseDTO(series);
    }

    /**
     * Lấy chi tiết series theo slug
     */
    @Transactional(readOnly = true)
    public SeriesResponseDTO getSeriesBySlug(String slug) {
        Series series = seriesRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Series not found with slug: " + slug));

        // Tăng view count
        series.incrementViewCount();
        seriesRepository.save(series);

        return mapToResponseDTO(series);
    }

    /**
     * Lấy danh sách series với phân trang
     */
    @Transactional(readOnly = true)
    public Page<SeriesListDTO> getAllSeries(Pageable pageable) {
        Page<Series> seriesPage = seriesRepository.findAll(pageable);
        return seriesPage.map(this::mapToListDTO);
    }

    /**
     * Lấy danh sách series của một tác giả
     */
    @Transactional(readOnly = true)
    public Page<SeriesListDTO> getSeriesByUserId(String userId, Pageable pageable) {
        Page<Series> seriesPage = seriesRepository.findByUserId(userId, pageable);
        return seriesPage.map(this::mapToListDTO);
    }

    /**
     * Tìm kiếm series
     */
    @Transactional(readOnly = true)
    public Page<SeriesListDTO> searchSeries(SeriesSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(
                searchDTO.getPage(),
                searchDTO.getSize(),
                Sort.by(Sort.Direction.fromString(searchDTO.getSortDirection()), searchDTO.getSortBy())
        );

        Page<Series> seriesPage;

        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
            seriesPage = seriesRepository.searchByKeyword(searchDTO.getKeyword(), pageable);
        } else if (searchDTO.getUserId() != null) {
            if (searchDTO.getIsActive() != null && searchDTO.getIsActive()) {
                seriesPage = seriesRepository.findByUserIdAndIsActiveTrue(searchDTO.getUserId(), pageable);
            } else {
                seriesPage = seriesRepository.findByUserId(searchDTO.getUserId(), pageable);
            }
        } else if (searchDTO.getIsActive() != null && searchDTO.getIsActive()) {
            seriesPage = seriesRepository.findByIsActiveTrue(pageable);
        } else if (searchDTO.getIsCompleted() != null && searchDTO.getIsCompleted()) {
            seriesPage = seriesRepository.findByIsCompletedTrue(pageable);
        } else {
            seriesPage = seriesRepository.findAll(pageable);
        }

        return seriesPage.map(this::mapToListDTO);
    }

    /**
     * Thêm bài viết vào series
     */
    public SeriesResponseDTO addPostToSeries(String seriesId, AddPostToSeriesDTO dto, String userId) throws BadRequestException {
        log.info("Adding post: {} to series: {}", dto.getPostId(), seriesId);

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("Series not found with id: " + seriesId));

        // Kiểm tra quyền sở hữu
        if (!series.getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have permission to modify this series");
        }

        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + dto.getPostId()));

        // Kiểm tra post đã có trong series chưa
        if (seriesPostRepository.existsBySeriesIdAndPostId(seriesId, dto.getPostId())) {
            throw new BadRequestException("Post already exists in this series");
        }

        // Xác định order index
        Integer orderIndex = dto.getOrderIndex();
        if (orderIndex == null) {
            // Thêm vào cuối
             orderIndex = seriesPostRepository
                    .findMaxOrderIndexBySeriesId(seriesId)
                    .orElse(0) + 1;
        } else {
            // Dịch chuyển các bài viết khác
            List<SeriesPost> affectedPosts = seriesPostRepository
                    .findBySeriesIdAndOrderIndexGreaterThan(seriesId, orderIndex - 1);
            affectedPosts.forEach(sp -> sp.setOrderIndex(sp.getOrderIndex() + 1));
            seriesPostRepository.saveAll(affectedPosts);
        }

        // Tạo SeriesPost mới
        SeriesPost seriesPost = SeriesPost.builder()
                .series(series)
                .post(post)
                .orderIndex(orderIndex)
                .build();

        seriesPostRepository.save(seriesPost);

        // Cập nhật totalPosts
        series.setTotalPosts(seriesPostRepository.countBySeriesId(seriesId).intValue());
        seriesRepository.save(series);

        log.info("Post added to series successfully");
        return mapToResponseDTO(series);
    }

    /**
     * Xóa bài viết khỏi series
     */
    public SeriesResponseDTO removePostFromSeries(String seriesId, String postId, String userId) throws BadRequestException {
        log.info("Removing post: {} from series: {}", postId, seriesId);

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("Series not found with id: " + seriesId));

        // Kiểm tra quyền sở hữu
        if (!series.getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have permission to modify this series");
        }

        SeriesPost seriesPost = seriesPostRepository.findBySeriesIdAndPostId(seriesId, postId)
                .orElseThrow(() -> new NotFoundException("Post not found in this series"));

        Integer removedOrderIndex = seriesPost.getOrderIndex();
        seriesPostRepository.delete(seriesPost);

        // Sắp xếp lại thứ tự
        List<SeriesPost> remainingPosts = seriesPostRepository
                .findBySeriesIdAndOrderIndexGreaterThan(seriesId, removedOrderIndex);
        remainingPosts.forEach(sp -> sp.setOrderIndex(sp.getOrderIndex() - 1));
        seriesPostRepository.saveAll(remainingPosts);

        // Cập nhật totalPosts
        series.setTotalPosts(seriesPostRepository.countBySeriesId(seriesId).intValue());
        seriesRepository.save(series);

        log.info("Post removed from series successfully");
        return mapToResponseDTO(series);
    }

    /**
     * Sắp xếp lại thứ tự bài viết trong series
     */
    public SeriesResponseDTO reorderPost(String seriesId, ReorderSeriesPostDTO dto, String userId) throws BadRequestException {
        log.info("Reordering post: {} in series: {}", dto.getPostId(), seriesId);

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("Series not found with id: " + seriesId));

        // Kiểm tra quyền sở hữu
        if (!series.getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have permission to modify this series");
        }

        SeriesPost seriesPost = seriesPostRepository.findBySeriesIdAndPostId(seriesId, dto.getPostId())
                .orElseThrow(() -> new NotFoundException("Post not found in this series"));

        Integer oldIndex = seriesPost.getOrderIndex();
        Integer newIndex = dto.getNewOrderIndex();

        if (oldIndex.equals(newIndex)) {
            return mapToResponseDTO(series);
        }

        // Lấy tất cả posts trong series
        List<SeriesPost> allPosts = seriesPostRepository.findBySeriesIdOrderByOrderIndexAsc(seriesId);

        if (newIndex < 1 || newIndex > allPosts.size()) {
            throw new BadRequestException("Invalid order index");
        }

        // Xóa post khỏi vị trí cũ
        allPosts.remove(seriesPost);

        // Chèn vào vị trí mới
        allPosts.add(newIndex - 1, seriesPost);

        // Cập nhật lại order index cho tất cả
        for (int i = 0; i < allPosts.size(); i++) {
            allPosts.get(i).setOrderIndex(i + 1);
        }

        seriesPostRepository.saveAll(allPosts);
        log.info("Post reordered successfully");

        return mapToResponseDTO(series);
    }

    /**
     * Xóa series
     */
    public void deleteSeries(String seriesId, String userId) throws BadRequestException {
        log.info("Deleting series: {} by user: {}", seriesId, userId);

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("Series not found with id: " + seriesId));

        // Kiểm tra quyền sở hữu
        if (!series.getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have permission to delete this series");
        }

        seriesRepository.delete(series);
        log.info("Series deleted successfully: {}", seriesId);
    }

    /**
     * Lấy các series phổ biến nhất
     */
    @Transactional(readOnly = true)
    public Page<SeriesListDTO> getMostViewedSeries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Series> seriesPage = seriesRepository.findMostViewed(pageable);
        return seriesPage.map(this::mapToListDTO);
    }

    // ========== Helper Methods ==========

    /**
     * Map Series entity sang SeriesResponseDTO
     */
    private SeriesResponseDTO mapToResponseDTO(Series series) {
        List<SeriesPostDTO> posts = seriesPostRepository
                .findBySeriesIdOrderByOrderIndexAsc(series.getId())
                .stream()
                .map(this::mapToSeriesPostDTO)
                .collect(Collectors.toList());

        return SeriesResponseDTO.builder()
                .id(series.getId())
                .title(series.getTitle())
                .slug(series.getSlug())
                .description(series.getDescription())
                .thumbnail(series.getThumbnail())
                .userId(series.getUser().getId())
                .username(series.getUser().getUsername())
                .userAvatar(series.getUser().getAvatar())
                .isActive(series.getIsActive())
                .isCompleted(series.getIsCompleted())
                .totalPosts(series.getTotalPosts())
                .viewCount(series.getViewCount())
                .createdAt(series.getCreatedAt())
                .updatedAt(series.getUpdatedAt())
                .posts(posts)
                .build();
    }

    /**
     * Map Series entity sang SeriesListDTO (không có posts)
     */
    private SeriesListDTO mapToListDTO(Series series) {
        return SeriesListDTO.builder()
                .id(series.getId())
                .title(series.getTitle())
                .slug(series.getSlug())
                .description(series.getDescription())
                .thumbnail(series.getThumbnail())
                .username(series.getUser().getUsername())
                .userAvatar(series.getUser().getAvatar())
                .isActive(series.getIsActive())
                .isCompleted(series.getIsCompleted())
                .totalPosts(series.getTotalPosts())
                .viewCount(series.getViewCount())
                .createdAt(series.getCreatedAt())
                .updatedAt(series.getUpdatedAt())
                .build();
    }

    /**
     * Map SeriesPost entity sang SeriesPostDTO
     */
    private SeriesPostDTO mapToSeriesPostDTO(SeriesPost seriesPost) {
        Post post = seriesPost.getPost();
        return SeriesPostDTO.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .thumbnail(post.getThumbnail())
                .orderIndex(seriesPost.getOrderIndex())
                .addedAt(seriesPost.getCreatedAt())
                .publicDate(post.getPublic_date())
                .build();
    }
}