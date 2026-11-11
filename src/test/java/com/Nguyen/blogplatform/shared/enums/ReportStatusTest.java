package com.Nguyen.blogplatform.shared.enums;

import org.junit.jupiter.api.Test;

import static com.Nguyen.blogplatform.shared.enums.ReportStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class ReportStatusTest {

    @Test
    void followsModerationWorkflow() {
        assertThat(PENDING.canTransitionTo(UNDER_REVIEW)).isTrue();
        assertThat(UNDER_REVIEW.canTransitionTo(RESOLVED)).isTrue();
        assertThat(UNDER_REVIEW.canTransitionTo(DISMISSED)).isTrue();
    }

    @Test
    void rejectsSkippingReviewAndReopening() {
        assertThat(PENDING.canTransitionTo(RESOLVED)).isFalse();
        assertThat(PENDING.canTransitionTo(DISMISSED)).isFalse();
        assertThat(RESOLVED.canTransitionTo(UNDER_REVIEW)).isFalse();
        assertThat(DISMISSED.canTransitionTo(UNDER_REVIEW)).isFalse();
        assertThat(DISMISSED.canTransitionTo(RESOLVED)).isFalse();
    }
}
