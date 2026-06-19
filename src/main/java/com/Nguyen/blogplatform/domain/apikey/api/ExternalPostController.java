package com.Nguyen.blogplatform.domain.apikey.api;



import com.Nguyen.blogplatform.domain.apikey.dto.ExternalPostResponse;
import com.Nguyen.blogplatform.domain.auth.application.UserDetailsImpl;
import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.shared.enums.PublishStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/external/posts")
@RequiredArgsConstructor
@Tag(name = "External API", description = "Endpoints for third-party platform integration using API Keys")
public class ExternalPostController {

    private final PostRepository postRepository;

    @GetMapping
    @Operation(summary = "Get user's public posts", description = "Fetch a paginated list of published posts belonging to the user identified by the API Key. Requires X-API-KEY and X-API-SECRET headers.")
    public ResponseEntity<Page<ExternalPostResponse>> getMyPublicPosts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        // Use a dummy user object with the ID from userDetails to avoid extra DB hit if possible,
        // but PostRepository expects a User entity. We can use Reference.
        com.Nguyen.blogplatform.domain.user.domain.model.User author = new com.Nguyen.blogplatform.domain.user.domain.model.User();
        author.setId(userDetails.getId());

        Page<Post> posts = postRepository.findByAuthorAndVisibility(
                author, PublishStatus.PUBLISHED, pageable);

        Page<ExternalPostResponse> response = posts.map(this::mapToExternalResponse);
        return ResponseEntity.ok(response);
    }

    private ExternalPostResponse mapToExternalResponse(Post post) {
        return new ExternalPostResponse(
                post.getId(),
                post.getTitle(),
                post.getExcerpt(),
                post.getSlug(),
                post.getContent(),
                post.getThumbnail(),
                post.getPublishedAt(),
                post.getAuthor().getUsername()
        );
    }
}
