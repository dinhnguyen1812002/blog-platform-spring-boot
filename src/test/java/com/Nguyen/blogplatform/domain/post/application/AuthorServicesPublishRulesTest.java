package com.Nguyen.blogplatform.domain.post.application;

import com.Nguyen.blogplatform.domain.notification.application.NotificationService;
import com.Nguyen.blogplatform.domain.post.domain.model.Category;
import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.dto.PostRequest;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.BookmarkRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.CategoryRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.TagRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import com.Nguyen.blogplatform.shared.enums.PublishStatus;
import com.Nguyen.blogplatform.shared.exception.ForbiddenException;
import com.Nguyen.blogplatform.shared.mapper.PostMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Content invariants:
 *  - editing a published post never resets publishedAt or re-notifies followers
 *  - a published post's slug is stable; a draft's slug follows its title
 *  - first publication sets publishedAt and notifies once
 */
@ExtendWith(MockitoExtension.class)
class AuthorServicesPublishRulesTest {

    @Mock PostRepository postRepository;
    @Mock UserRepository userRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock TagRepository tagRepository;
    @Mock BookmarkRepository bookmarkRepository;
    @Mock PostMapper postMapper;
    @Mock NotificationService notificationService;

    @InjectMocks AuthorServices authorServices;

    private static final LocalDateTime FIRST_PUBLISHED = LocalDateTime.of(2025, 1, 5, 9, 0);

    private User author;

    @BeforeEach
    void setUp() {
        author = new User("alice", "alice@example.com", "x");
        author.setId("u1");
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("alice", null, "ROLE_USER"));
        lenient().when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(postRepository.existsBySlugAndIdNot(anyString(), anyString())).thenReturn(false);
        lenient().when(categoryRepository.findAllById(any())).thenReturn(java.util.List.of(mock(Category.class)));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Post post(PublishStatus visibility, LocalDateTime publishedAt) {
        Post post = Post.builder()
                .title("Đường đi Đà Nẵng")
                .slug("duong-di-da-nang")
                .content("<p>content</p>")
                .author(author)
                .visibility(visibility)
                .publishedAt(publishedAt)
                .build();
        post.setId("p1");
        when(postRepository.findById("p1")).thenReturn(Optional.of(post));
        return post;
    }

    private PostRequest request(String title, PublishStatus visibility) {
        return new PostRequest(title, "excerpt text", "<p>some content</p>", null,
                Set.of(1L), Set.of(), null, visibility, null, null);
    }

    @Test
    void editingPublishedPostKeepsPublishedAtSlugAndDoesNotNotify() {
        Post post = post(PublishStatus.PUBLISHED, FIRST_PUBLISHED);

        authorServices.updatePost("p1", request("Kinh nghiệm du lịch Đà Nẵng", PublishStatus.PUBLISHED));

        assertThat(post.getPublishedAt()).isEqualTo(FIRST_PUBLISHED);
        assertThat(post.getSlug()).isEqualTo("duong-di-da-nang");
        assertThat(post.getTitle()).isEqualTo("Kinh nghiệm du lịch Đà Nẵng");
        verifyNoInteractions(notificationService);
    }

    @Test
    void draftSlugFollowsTitle() {
        Post post = post(PublishStatus.DRAFT, null);

        authorServices.updatePost("p1", request("Kinh nghiệm du lịch Đà Nẵng", PublishStatus.DRAFT));

        assertThat(post.getSlug()).isEqualTo("kinh-nghiem-du-lich-da-nang");
    }

    @Test
    void firstPublicationSetsPublishedAtAndNotifiesOnce() {
        Post post = post(PublishStatus.DRAFT, null);

        authorServices.updatePost("p1", request("Đường đi Đà Nẵng", PublishStatus.PUBLISHED));

        assertThat(post.getPublishedAt()).isNotNull().isBeforeOrEqualTo(LocalDateTime.now());
        verify(notificationService).createUserNotification(eq("u1"), eq("POST_PUBLISHED"), anyString(), anyString());
    }

    @Test
    void unpublishingKeepsFirstPublicationDate() {
        Post post = post(PublishStatus.PUBLISHED, FIRST_PUBLISHED);

        authorServices.updatePost("p1", request("Đường đi Đà Nẵng", PublishStatus.DRAFT));

        assertThat(post.getVisibility()).isEqualTo(PublishStatus.DRAFT);
        assertThat(post.getPublishedAt()).isEqualTo(FIRST_PUBLISHED);
    }

    @Test
    void republishingAfterUnpublishDoesNotRenotify() {
        Post post = post(PublishStatus.DRAFT, FIRST_PUBLISHED); // was published, then unpublished

        authorServices.updatePost("p1", request("Đường đi Đà Nẵng", PublishStatus.PUBLISHED));

        assertThat(post.getPublishedAt()).isEqualTo(FIRST_PUBLISHED);
        verifyNoInteractions(notificationService);
    }

    @Test
    void adminCanManageAnotherUsersPost() {
        Post post = post(PublishStatus.DRAFT, null);
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("root", null, "ROLE_USER", "ROLE_ADMIN"));

        authorServices.updatePost("p1", request("Admin đã chỉnh sửa", PublishStatus.DRAFT));

        assertThat(post.getTitle()).isEqualTo("Admin đã chỉnh sửa");
        assertThat(post.getAuthor()).isSameAs(author); // ownership unchanged
    }

    @Test
    void otherUsersCannotManageSomeoneElsesPost() {
        post(PublishStatus.DRAFT, null);
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("mallory", null, "ROLE_USER"));

        assertThatThrownBy(() -> authorServices.updatePost("p1", request("Hijacked", PublishStatus.DRAFT)))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> authorServices.deletePost("p1"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void publishedPostCannotBeScheduledAgain() {
        post(PublishStatus.PUBLISHED, FIRST_PUBLISHED);
        PostRequest req = new PostRequest("Đường đi Đà Nẵng", "excerpt text", "<p>some content</p>", null,
                Set.of(1L), Set.of(), null, PublishStatus.SCHEDULED, LocalDateTime.now().plusDays(1), null);

        assertThatThrownBy(() -> authorServices.updatePost("p1", req))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
