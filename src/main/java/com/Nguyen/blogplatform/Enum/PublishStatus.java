package com.Nguyen.blogplatform.Enum;

public enum PublishStatus {
    PUBLISHED, // Immediately visible to all users
    SCHEDULED, // Visible to all users at a specified future time
    DRAFT, // Not published yet (work in progress)
    PRIVATE // Visible only to the author
}
