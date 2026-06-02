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
 * Links a {@link Teacher} to a {@link Subject} they teach. Carries whether the
 * assignment is currently {@code active} and a {@code goingAgainst} counter of student
 * reports disputing it.
 */
@Entity
@Table(name = "teacher_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "teacher_id"}))
public class TeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int goingAgainst;

    protected TeacherAssignment() {
        // for JPA
    }

    public TeacherAssignment(Subject subject, Teacher teacher, boolean active) {
        this.subject = subject;
        this.teacher = teacher;
        this.active = active;
        this.goingAgainst = 0;
    }

    public Long getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.goingAgainst = 0;
    }

    public int getGoingAgainst() {
        return goingAgainst;
    }

    public void incrementGoingAgainst() {
        this.goingAgainst++;
    }

    public void resetGoingAgainst() {
        this.goingAgainst = 0;
    }
}
