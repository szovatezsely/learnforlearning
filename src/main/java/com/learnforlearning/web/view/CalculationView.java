package com.learnforlearning.web.view;

/** A past recommendation, with the subject resolved for display and linking. */
public record CalculationView(Long subjectId, String subjectName, String subjectCode) {
}
