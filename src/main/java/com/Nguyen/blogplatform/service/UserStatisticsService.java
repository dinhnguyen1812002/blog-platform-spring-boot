package com.Nguyen.blogplatform.service;

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

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatisticsService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostService postService;
    private final PostMapper postMapper;
    private final BookmarkRepository bookmarkRepository;

    /**
     * Get the most viewed posts for a user
     * @param userId the user ID
     * @param limit the maximum number of posts to return
     * @return a list of the most viewed posts as PostResponse objects
     */
    public List<PostResponse> getMostViewedPosts(String userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Pageable pageable = PageRequest.of(0, limit, Sort.by("view").descending());
        List<Post> posts = postRepository.findByUser(user, pageable).getContent();

        return posts.stream()
                .map(post -> postMapper.toPostResponse(post, user, bookmarkRepository))
                .collect(Collectors.toList());
    }

    /**
     * Get the most liked posts for a user
     * @param userId the user ID
     * @param limit the maximum number of posts to return
     * @return a list of the most liked posts as PostResponse objects
     */
    public List<PostResponse> getMostLikedPosts(String userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Get all posts by the user
        List<Post> userPosts = postRepository.findByUser(user);

        // Sort by like count and limit the result
        List<Post> mostLikedPosts = userPosts.stream()
                .sorted(Comparator.comparing(post -> post.getLike().size(), Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toList());

        return mostLikedPosts.stream()
                .map(post -> postMapper.toPostResponse(post, user, bookmarkRepository))
                .collect(Collectors.toList());
    }

    /**
     * Get the total post count for a user
     * @param userId the user ID
     * @return the total post count
     */
    public long getTotalPostCount(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        return postRepository.countByUser(user);
    }

    /**
     * Get traffic statistics for a user
     * @param userId the user ID
     * @return a map containing traffic statistics
     */
    public Map<String, Object> getTrafficStatistics(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Post> userPosts = postRepository.findByUser(user);

        long totalViews = userPosts.stream().mapToLong(Post::getView).sum();
        long totalLikes = userPosts.stream().mapToLong(post -> post.getLike().size()).sum();
        long totalComments = userPosts.stream().mapToLong(post -> post.getComments().size()).sum();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalPosts", userPosts.size());
        statistics.put("totalViews", totalViews);
        statistics.put("totalLikes", totalLikes);
        statistics.put("totalComments", totalComments);
        statistics.put("averageViewsPerPost", userPosts.isEmpty() ? 0 : totalViews / userPosts.size());
        statistics.put("averageLikesPerPost", userPosts.isEmpty() ? 0 : totalLikes / userPosts.size());
        statistics.put("averageCommentsPerPost", userPosts.isEmpty() ? 0 : totalComments / userPosts.size());

        return statistics;
    }

    /**
     * Get monthly read statistics for a user
     * @param userId the user ID
     * @param year the year to get statistics for
     * @return a map containing monthly read statistics
     */
    public Map<String, Long> getMonthlyReadStatistics(String userId, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Post> userPosts = postRepository.findByUser(user);

        Map<Month, Long> monthlyViews = new HashMap<>();
        for (Month month : Month.values()) {
            monthlyViews.put(month, 0L);
        }

        for (Post post : userPosts) {
            Date createdAt = post.getCreatedAt();
            LocalDate localDate = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (localDate.getYear() == year) {
                Month month = localDate.getMonth();
                monthlyViews.put(month, monthlyViews.get(month) + post.getView());
            }
        }

        Map<String, Long> result = new HashMap<>();
        for (Month month : Month.values()) {
            result.put(month.toString(), monthlyViews.get(month));
        }

        return result;
    }

    /**
     * Get yearly read statistics for a user
     * @param userId the user ID
     * @param startYear the start year
     * @param endYear the end year
     * @return a map containing yearly read statistics
     */
    public Map<Integer, Long> getYearlyReadStatistics(String userId, int startYear, int endYear) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Post> userPosts = postRepository.findByUser(user);

        Map<Integer, Long> yearlyViews = new HashMap<>();
        for (int year = startYear; year <= endYear; year++) {
            yearlyViews.put(year, 0L);
        }

        for (Post post : userPosts) {
            Date createdAt = post.getCreatedAt();
            LocalDate localDate = createdAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int year = localDate.getYear();

            if (year >= startYear && year <= endYear) {
                yearlyViews.put(year, yearlyViews.get(year) + post.getView());
            }
        }

        return yearlyViews;
    }

    /**
     * Get comprehensive statistics for a user
     * @param userId the user ID
     * @param limit the maximum number of posts to return for most viewed and most liked
     * @param year the year to get monthly statistics for
     * @param startYear the start year for yearly statistics
     * @param endYear the end year for yearly statistics
     * @return a UserStatisticsResponse containing all statistics
     */
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
