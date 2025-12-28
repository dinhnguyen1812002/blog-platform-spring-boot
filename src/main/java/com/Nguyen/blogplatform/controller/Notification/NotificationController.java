package com.Nguyen.blogplatform.controller.Notification;



import com.Nguyen.blogplatform.model.Notifications;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.NotificationRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.service.NotificationService;
import com.Nguyen.blogplatform.service.UserServices;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationService  notificationService;


    @GetMapping
    public List<Notifications> getNotificationsOfUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationService.getNotificationsOfUser(user.getId());
    }

    @PutMapping("/{id}/read")
    public Notifications markAsRead(@PathVariable String id) {

        var noti = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        noti.setIsRead(true);
        return notificationRepository.save(noti);
    }

    @PutMapping("/read-all")
    public void markAllAsRead() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationRepository.markAllAsRead(user.getId());
    }
}



