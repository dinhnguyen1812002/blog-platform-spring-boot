package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.Notifications;
import com.Nguyen.blogplatform.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notifications, String> {


    @Modifying
    @Query("UPDATE Notifications n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllAsRead(@Param("userId") String userId);

    List<Notifications> findByUser_IdOrderByCreatedAtDesc(String userId);

    Optional<Notifications> findById(String id);
}
