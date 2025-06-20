package com.Nguyen.blogplatform.mapper;



import com.Nguyen.blogplatform.context.UserContextService;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.Rating;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.Nguyen.blogplatform.Utils.ExcerptUtil.excerpt;

@Component
public class PostMapper {

    private final UserContextService userContextService;
    private final CommentMapper commentMapper;

    public PostMapper(UserContextService userContextService, CommentMapper commentMapper) {
        this.userContextService = userContextService;
        this.commentMapper = commentMapper;
    }


}

