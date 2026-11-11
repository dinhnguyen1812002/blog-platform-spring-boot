package com.Nguyen.blogplatform.shared.enums;



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
    DISMISSED;

    /**
     * Allowed workflow: PENDING → UNDER_REVIEW → (RESOLVED | DISMISSED).
     * RESOLVED and DISMISSED are terminal; reopening needs its own audited action.
     */
    public boolean canTransitionTo(ReportStatus next) {
        return switch (this) {
            case PENDING -> next == UNDER_REVIEW;
            case UNDER_REVIEW -> next == RESOLVED || next == DISMISSED;
            case RESOLVED, DISMISSED -> false;
        };
    }
}
