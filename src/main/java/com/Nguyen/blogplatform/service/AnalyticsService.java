package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.payload.response.AnalyticsResponse;
import com.Nguyen.blogplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private NewsletterRepository newsletterRepository;



    public AnalyticsResponse analytics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth.minusSeconds(1);

        // Total hiện tại
        long totalUser = userRepository.count();
        long totalPost = postRepository.count();
        long totalTag = tagRepository.count();
        long totalCategory = categoryRepository.count();
        long totalSubscribers = newsletterRepository.countByIsActiveTrueAndIsConfirmedTrue();

        // Thêm mới tháng này
        long newUsersThisMonth = userRepository.countByCreatedAtBetween(startOfThisMonth, now);
        long newPostsThisMonth = postRepository.countByCreatedAtBetween(startOfThisMonth, now);
        long newSubsThisMonth = newsletterRepository
                .countBySubscribedAtBetweenAndIsActiveTrueAndIsConfirmedTrue(startOfThisMonth, now);

        // Thêm mới tháng trước
        long newUsersLastMonth = userRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        long newPostsLastMonth = postRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        long newSubsLastMonth = newsletterRepository
                .countBySubscribedAtBetweenAndIsActiveTrueAndIsConfirmedTrue(startOfLastMonth, endOfLastMonth);

        return AnalyticsResponse.builder()
                .totalUsers(totalUser)
                .totalPosts(totalPost)
                .totalTags(totalTag)
                .totalCategories(totalCategory)
                .totalSubscribers(totalSubscribers)
                .userGrowth(calcGrowth(newUsersThisMonth, newUsersLastMonth))
                .postGrowth(calcGrowth(newPostsThisMonth, newPostsLastMonth))
                .subscriberGrowth(calcGrowth(newSubsThisMonth, newSubsLastMonth))
                .build();
    }
    private double calcGrowth(long thisMonth, long lastMonth) {
        if (lastMonth == 0) {
            return thisMonth > 0 ? 100.0 : 0.0; // tránh chia 0
        }
        return ((double) (thisMonth - lastMonth) / lastMonth) * 100;
    }
}
