package com.Nguyen.blogplatform.mapper;


import com.Nguyen.blogplatform.context.UserContextService;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public PostMapper(UserContextService userContextService, CommentMapper commentMapper) {
    }


}

