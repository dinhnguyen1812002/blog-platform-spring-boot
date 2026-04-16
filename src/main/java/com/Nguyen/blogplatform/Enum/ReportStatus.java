package com.Nguyen.blogplatform.Enum;

/**
 * Status of an article report in the moderation workflow.
 * Tracks the lifecycle of a report from submission to resolution.
 */
public enum ReportStatus {
    /**
     * Report has been submitted and is awaiting review
     */
    PENDING,
    
    /**
     * Report is currently being reviewed by a moderator
     */
    UNDER_REVIEW,
    
    /**
     * Report has been validated and appropriate action has been taken
     * (e.g., content removed, warning issued to author)
     */
    RESOLVED,
    
    /**
     * Report has been reviewed and dismissed as invalid or unfounded
     */
    DISMISSED
}
