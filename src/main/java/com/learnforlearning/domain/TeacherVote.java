package com.learnforlearning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A student's rating of and/or comment on a teacher. Either field may be null: a row
 * can be a vote only, a comment only, or both.
 */
@Entity
@Table(name = "teacher_votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "user_id"}))
public class TeacherVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** {@code true} = up-vote, {@code false} = down-vote, {@code null} = no vote (comment only). */
    @Column(name = "positive_vote")
    private Boolean positiveVote;

    @Column(length = 2000)
    private String comment;

    protected TeacherVote() {
        // for JPA
    }

    public TeacherVote(Teacher teacher, User user) {
        this.teacher = teacher;
        this.user = user;
    }

    /** True when this row carries neither a vote nor a comment and can be removed. */
    public boolean isEmpty() {
        return positiveVote == null && (comment == null || comment.isBlank());
    }

    public Long getId() {
        return id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public User getUser() {
        return user;
    }

    public Boolean getPositiveVote() {
        return positiveVote;
    }

    public void setPositiveVote(Boolean positiveVote) {
        this.positiveVote = positiveVote;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
