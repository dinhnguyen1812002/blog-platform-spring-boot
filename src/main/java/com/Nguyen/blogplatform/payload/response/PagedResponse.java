package com.Nguyen.blogplatform.payload.response;

import lombok.Data;

import java.util.List;
@Data
public class PagedResponse<T> {
    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    // Constructor
    public PagedResponse(List<T> data, int page, int size, long totalElements, int totalPages, boolean last) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }


}

