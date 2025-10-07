package com.Nguyen.blogplatform.model;

import com.Nguyen.blogplatform.Enum.ESocialMediaPlatform;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "social_media_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialMediaLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ESocialMediaPlatform platform;

    @Column(nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    // ✅ Đảm bảo mỗi user chỉ có 1 link cho mỗi platform
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocialMediaLink)) return false;
        SocialMediaLink that = (SocialMediaLink) o;
        return platform == that.platform &&
                user != null &&
                user.getId() != null &&
                user.getId().equals(that.user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, user != null ? user.getId() : null);
    }
}
