package com.Nguyen.blogplatform.model;

import de.huxhorn.sulky.ulid.ULID;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notifications {

    @Id
    private String notificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Size(max = 50)
    private String type;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private Boolean isRead = false;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.notificationId == null) {
            this.notificationId = new ULID().nextULID();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isRead == null) {
            this.isRead = false;
        }
    }
}
