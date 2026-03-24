package com.Nguyen.blogplatform.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalSearchResponse {
    private List<SearchItemDTO> posts;
    private List<SearchItemDTO> series;
    private List<SearchItemDTO> users;
}
