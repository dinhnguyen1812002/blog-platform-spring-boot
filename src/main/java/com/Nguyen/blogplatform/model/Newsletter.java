package com.Nguyen.blogplatform.model;


import com.Nguyen.blogplatform.Enum.NewsletterFrequency;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "newsletter_subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Newsletter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Email
    @NotEmpty
    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isConfirmed = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NewsletterFrequency frequency = NewsletterFrequency.DAILY;

    @Column(unique = true)
    private String subscriptionToken;

    private String confirmationToken;

    @CreationTimestamp
    private LocalDateTime subscribedAt;

    private LocalDateTime confirmedAt;

    private LocalDateTime unsubscribedAt;

    private LocalDateTime lastSentAt;
}
