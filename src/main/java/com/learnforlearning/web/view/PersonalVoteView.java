package com.learnforlearning.web.view;

/** A row on the student's personal page: their vote and/or comment about a teacher. */
public record PersonalVoteView(Long teacherId, String teacherName, String comment, Boolean positiveVote) {

    public boolean hasComment() {
        return comment != null && !comment.isBlank();
    }
}
