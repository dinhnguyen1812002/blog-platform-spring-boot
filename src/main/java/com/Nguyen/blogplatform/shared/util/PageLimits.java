package com.Nguyen.blogplatform.shared.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class PageLimits {
    private PageLimits() {}
    public static int size(int size) {
        if (size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be between 1 and 100");
        return size;
    }
    public static int page(int page) {
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be non-negative");
        return page;
    }
}
