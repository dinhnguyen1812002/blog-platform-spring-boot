package com.Nguyen.blogplatform.payload.response.newsletter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {

    private int imported;
    private int skipped;
    private int failed;
    private List<String> errors;
}
