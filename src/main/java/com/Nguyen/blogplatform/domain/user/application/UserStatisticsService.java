package com.Nguyen.blogplatform.domain.user.application;



import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.dto.PostResponse;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.BookmarkRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.dto.UserStatisticsResponse;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import com.Nguyen.blogplatform.shared.exception.NotFoundException;
import com.Nguyen.blogplatform.shared.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserStatisticsService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final BookmarkRepository bookmarkRepository;

    private static final int MAX_LIMIT = 100;

    private User requireUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }

    private int validateLimit(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "limit must be between 1 and 100");
        return limit;
    }

    private List<PostResponse> mapPosts(List<Post> posts, User user) {
        if (posts.isEmpty()) return List.of();
        Set<String> saved = bookmarkRepository.findBookmarkedPostIds(user, posts);
        var responses = postMapper.toPostResponses(posts, user, saved, false);
        return posts.stream().map(post -> responses.get(post.getId())).toList();
    }

    public List<PostResponse> getMostViewedPosts(String userId, int limit) {
        User user = requireUser(userId);
        Pageable pageable = PageRequest.of(0, validateLimit(limit), Sort.by("viewCount").descending());
        return mapPosts(postRepository.findByAuthor(user, pageable).getContent(), user);
    }

    public List<PostResponse> getMostLikedPosts(String userId, int limit) {
        User user = requireUser(userId);
        return mapPosts(postRepository.findMostLikedByAuthor(user, PageRequest.of(0, validateLimit(limit))), user);
    }

    public long getTotalPostCount(String userId) {
        return postRepository.countByAuthor(requireUser(userId));
    }

    public Map<String, Object> getTrafficStatistics(String userId) {
        requireUser(userId);
        Object[] row = postRepository.findAuthorTraffic(userId);
        // Some JPA providers wrap aggregate rows in a one-element array.
        if (row.length == 1 && row[0] instanceof Object[] nested) row = nested;
        long count = ((Number) row[0]).longValue();
        long views = ((Number) row[1]).longValue();
        long likes = ((Number) row[2]).longValue();
        long comments = ((Number) row[3]).longValue();
        Map<String, Object> result = new HashMap<>();
        result.put("totalPosts", count); result.put("totalViews", views);
        result.put("totalLikes", likes); result.put("totalComments", comments);
        result.put("averageViewsPerPost", count == 0 ? 0 : (double) views / count);
        result.put("averageLikesPerPost", count == 0 ? 0 : (double) likes / count);
        result.put("averageCommentsPerPost", count == 0 ? 0 : (double) comments / count);
        return result;
    }

    public Map<String, Long> getMonthlyReadStatistics(String userId, int year) {
        requireUser(userId);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Month month : Month.values()) result.put(month.toString(), 0L);
        for (Object[] row : postRepository.findAuthorMonthlyViews(userId, year))
            result.put(Month.of(((Number) row[0]).intValue()).toString(), ((Number) row[1]).longValue());
        return result;
    }

    public Map<Integer, Long> getYearlyReadStatistics(String userId, int startYear, int endYear) {
        if (startYear < 1 || endYear > 9999 || startYear > endYear || endYear - startYear > 100)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid year range");
        requireUser(userId);
        Map<Integer, Long> result = new TreeMap<>();
        for (int year = startYear; year <= endYear; year++) result.put(year, 0L);
        for (Object[] row : postRepository.findAuthorYearlyViews(userId, startYear, endYear))
            result.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        return result;
    }

    public UserStatisticsResponse getUserStatistics(String userId, int limit, int year, int startYear, int endYear) {
        User user = requireUser(userId);
        var traffic = getTrafficStatistics(userId);
        return UserStatisticsResponse.builder().userId(userId).username(user.getUsername())
                .totalPosts(((Number) traffic.get("totalPosts")).longValue())
                .mostViewedPosts(getMostViewedPosts(userId, limit)).mostLikedPosts(getMostLikedPosts(userId, limit))
                .trafficStatistics(traffic).monthlyReadStatistics(getMonthlyReadStatistics(userId, year))
                .yearlyReadStatistics(getYearlyReadStatistics(userId, startYear, endYear)).build();
    }
}
