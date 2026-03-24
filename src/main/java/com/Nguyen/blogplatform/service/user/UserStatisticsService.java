package com.Nguyen.blogplatform.service.user;

import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.mapper.PostMapper;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.UserStatisticsResponse;
import com.Nguyen.blogplatform.repository.BookmarkRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
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
@RequiredArgsConstructor
public class UserStatisticsService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final BookmarkRepository bookmarkRepository;

    public List<PostResponse> getMostViewedPosts(String userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Pageable pageable = PageRequest.of(0, limit, Sort.by("viewCount").descending());
        List<Post> posts = postRepository.findByAuthor(user, pageable).getContent();

        Set<String> bookmarkedPostIds = bookmarkRepository.findBookmarkedPostIds(user, posts);

        return posts.stream()
                .map(post -> postMapper.toPostResponse(post, user, bookmarkedPostIds))
                .collect(Collectors.toList());
    }

    public List<PostResponse> getMostLikedPosts(String userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Post> userPosts = postRepository.findByAuthor(user);
        
        // Find bookmarked IDs for these posts
        Set<String> bookmarkedPostIds = bookmarkRepository.findBookmarkedPostIds(user, userPosts);

        return userPosts.stream()
                .sorted(Comparator.comparing(post -> post.getLikes().size(), Comparator.reverseOrder()))
                .limit(limit)
                .map(post -> postMapper.toPostResponse(post, user, bookmarkedPostIds))
                .collect(Collectors.toList());
    }

    public long getTotalPostCount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        return postRepository.countByAuthor(user);
    }

    public Map<String, Object> getTrafficStatistics(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Post> userPosts = postRepository.findByAuthor(user);

        long totalViews = userPosts.stream().mapToLong(Post::getViewCount).sum();
        long totalLikes = userPosts.stream().mapToLong(post -> post.getLikes().size()).sum();
        long totalComments = userPosts.stream().mapToLong(post -> post.getComments().size()).sum();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalPosts", userPosts.size());
        statistics.put("totalViews", totalViews);
        statistics.put("totalLikes", totalLikes);
        statistics.put("totalComments", totalComments);
        statistics.put("averageViewsPerPost", userPosts.isEmpty() ? 0 : (double) totalViews / userPosts.size());
        statistics.put("averageLikesPerPost", userPosts.isEmpty() ? 0 : (double) totalLikes / userPosts.size());
        statistics.put("averageCommentsPerPost", userPosts.isEmpty() ? 0 : (double) totalComments / userPosts.size());

        return statistics;
    }

    public Map<String, Long> getMonthlyReadStatistics(String userId, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Post> userPosts = postRepository.findByAuthor(user);

        Map<String, Long> monthlyViews = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            monthlyViews.put(month.toString(), 0L);
        }

        for (Post post : userPosts) {
            LocalDateTime createdAt = post.getCreatedAt();
            if (createdAt != null && createdAt.getYear() == year) {
                String monthName = createdAt.getMonth().toString();
                monthlyViews.put(monthName, monthlyViews.get(monthName) + post.getViewCount());
            }
        }

        return monthlyViews;
    }

    public Map<Integer, Long> getYearlyReadStatistics(String userId, int startYear, int endYear) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Post> userPosts = postRepository.findByAuthor(user);

        Map<Integer, Long> yearlyViews = new TreeMap<>();
        for (int year = startYear; year <= endYear; year++) {
            yearlyViews.put(year, 0L);
        }

        for (Post post : userPosts) {
            LocalDateTime createdAt = post.getCreatedAt();
            if (createdAt != null) {
                int year = createdAt.getYear();
                if (year >= startYear && year <= endYear) {
                    yearlyViews.put(year, yearlyViews.get(year) + post.getViewCount());
                }
            }
        }

        return yearlyViews;
    }

    public UserStatisticsResponse getUserStatistics(String userId, int limit, int year, int startYear, int endYear) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<PostResponse> mostViewedPosts = getMostViewedPosts(userId, limit);
        List<PostResponse> mostLikedPosts = getMostLikedPosts(userId, limit);
        long totalPosts = getTotalPostCount(userId);
        Map<String, Object> trafficStats = getTrafficStatistics(userId);
        Map<String, Long> monthlyStats = getMonthlyReadStatistics(userId, year);
        Map<Integer, Long> yearlyStats = getYearlyReadStatistics(userId, startYear, endYear);

        return UserStatisticsResponse.builder()
                .userId(userId)
                .username(user.getUsername())
                .totalPosts(totalPosts)
                .mostViewedPosts(mostViewedPosts)
                .mostLikedPosts(mostLikedPosts)
                .trafficStatistics(trafficStats)
                .monthlyReadStatistics(monthlyStats)
                .yearlyReadStatistics(yearlyStats)
                .build();
    }
}
