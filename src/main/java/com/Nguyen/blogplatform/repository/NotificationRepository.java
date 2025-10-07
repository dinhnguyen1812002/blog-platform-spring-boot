package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.Notifications;
import com.Nguyen.blogplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notifications, String> {
    List<Notifications> findByUserOrderByCreatedAtDesc(User user);
    List<Notifications> findByUserAndIsReadFalse(User user);
}
