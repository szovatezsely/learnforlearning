package com.learnforlearning.web.view;

import com.learnforlearning.domain.Subject;

/**
 * JSON payload returned to the find page's AJAX call.
 *
 * @param successful whether a subject could be recommended
 * @param subject    the recommended subject (null when {@code successful} is false)
 */
public record CalculationResponse(boolean successful, SubjectPayload subject) {

    public record SubjectPayload(Long id, String name, String code) {
    }

    public static CalculationResponse of(Subject subject) {
        return new CalculationResponse(true,
                new SubjectPayload(subject.getId(), subject.getName(), subject.getCode()));
    }

    public static CalculationResponse failed() {
        return new CalculationResponse(false, null);
    }
}
