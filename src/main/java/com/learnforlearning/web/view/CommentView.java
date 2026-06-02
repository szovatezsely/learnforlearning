package com.learnforlearning.web.view;

/**
 * A comment as shown in the UI. {@code positiveVote} is {@code null} when the row
 * carries no vote, true for an up-vote, false for a down-vote.
 */
public record CommentView(String author, String comment, Boolean positiveVote) {

    public boolean hasComment() {
        return comment != null && !comment.isBlank();
    }
}
