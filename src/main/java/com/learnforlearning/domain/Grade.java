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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * A grade a student earned in a subject. Modelled as its own entity because the
 * student–subject link carries a payload (the {@code grade} value).
 */
@Entity
@Table(name = "grades", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "subject_id"}))
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    /** Hungarian academic grade, 1 (fail) .. 5 (excellent). ("value" is reserved in some DBs.) */
    @Column(name = "grade_value", nullable = false)
    private int value;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    protected Grade() {
        // for JPA
    }

    public Grade(User user, Subject subject, int value) {
        this.user = user;
        this.subject = subject;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Subject getSubject() {
        return subject;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
