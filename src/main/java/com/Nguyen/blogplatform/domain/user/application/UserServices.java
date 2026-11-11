package com.Nguyen.blogplatform.domain.user.application;



import com.Nguyen.blogplatform.infrastructure.security.TokenVersionService;
import com.Nguyen.blogplatform.domain.auth.infrastructure.repository.RefreshTokenRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.Role;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.dto.TopUserResponse;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import com.Nguyen.blogplatform.shared.exception.TokenExpiredException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServices {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TokenVersionService tokenVersionService;
    @Value("${frontend-url}")
    private String frontendUrl;

    // Modify sendResetEmail method
    public void sendResetEmail(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not Found with email"+ email));;

        user.setResetToken(UUID.randomUUID().toString());
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Use the base URL for the reset link
        // Link to the SPA reset page (frontend reads ?token= and calls POST /api/v1/auth/reset-password)
        String resetUrl = frontendUrl + "/reset-password?token=" + user.getResetToken();

        Context context = new Context();
        context.setVariable("resetUrl", resetUrl);
        String htmlContent = templateEngine.process("reset-password-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("noreply@Blog.com");
        helper.setTo(user.getEmail());
        helper.setSubject("Password Reset Request");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token);
        if (user == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token is invalid or expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        // Whoever had the old password (or a stolen session) must sign in again
        refreshTokenRepository.deleteAllByUserId(user.getId());
        tokenVersionService.revokeAll(user.getId());
    }

    public void updatePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        // Invalidate every access token issued with the old password. The caller issues a
        // fresh refresh cookie, so the current device silently refreshes and stays signed in.
        tokenVersionService.revokeAll(user.getId());
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not Found with email"+ email));
    }
    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(()->new UsernameNotFoundException("User not Found with email"+ id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(()->new UsernameNotFoundException("User not Found with username: "+ username));
    }
//    public User findById(String id) {
//        return userRepository.findById(id).orElse(null);
//    }

    public Set<Role> getRolesByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getRoles();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<TopUserResponse> getTopUser(int limit) {
        if (limit <= 0) return List.of();
        var rows = userRepository.findTopUserIdsByPostCount(org.springframework.data.domain.PageRequest.of(0, Math.min(limit, 100)));
        if (rows.isEmpty()) return List.of();
        var ids = rows.stream().map(row -> (String) row[0]).toList();
        var users = userRepository.findAllWithSocialLinksByIdIn(ids).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));
        return rows.stream().map(row -> {
            User user = users.get((String) row[0]);
            return new TopUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getAvatar(),
                    user.getBio(), ((Number) row[1]).longValue(), user.getSocialMediaLinks());
        }).toList();
    }

}

