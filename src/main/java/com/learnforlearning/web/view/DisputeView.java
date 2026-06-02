package com.learnforlearning.web.view;

import com.learnforlearning.domain.Teacher;

/** A pending teacher-activity dispute shown on the admin management page. */
public record DisputeView(Teacher teacher, Long subjectId, String subjectName,
                          boolean active, int goingAgainst) {
}
