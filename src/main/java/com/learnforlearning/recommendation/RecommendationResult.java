package com.learnforlearning.recommendation;

import com.learnforlearning.domain.Subject;

/**
 * Outcome of a recommendation run: either a suggested subject, or a failure when
 * there was nothing to suggest (no candidates, no usable training data, etc.).
 */
public record RecommendationResult(boolean successful, Subject subject) {

    public static RecommendationResult success(Subject subject) {
        return new RecommendationResult(true, subject);
    }

    public static RecommendationResult failure() {
        return new RecommendationResult(false, null);
    }
}
