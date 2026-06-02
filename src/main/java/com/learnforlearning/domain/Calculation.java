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
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A historical record of a subject the recommender suggested to a student.
 */
@Entity
@Table(name = "calculations")
public class Calculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String subjectCode;

    @CreationTimestamp
    private Instant createdAt;

    protected Calculation() {
        // for JPA
    }

    public Calculation(User user, String subjectCode) {
        this.user = user;
        this.subjectCode = subjectCode;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
