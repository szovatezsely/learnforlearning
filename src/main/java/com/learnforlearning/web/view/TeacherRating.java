package com.learnforlearning.web.view;

import com.learnforlearning.domain.Teacher;

/**
 * A teacher row on a subject page: their net score plus whether the current user
 * has already voted them up or down.
 */
public record TeacherRating(Teacher teacher, int points, boolean userUpvoted, boolean userDownvoted) {
}
