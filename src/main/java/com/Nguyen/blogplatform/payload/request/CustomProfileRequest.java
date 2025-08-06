package com.Nguyen.blogplatform.payload.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomProfileRequest {

    @Size(max = 10000, message = "Nội dung profile không được vượt quá 10000 ký tự")
    private String markdownContent;
}
