package com.learnforlearning.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * An instructor who can be assigned to subjects and rated/commented on by students.
 */
@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean accepted;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherVote> votes = new ArrayList<>();

    protected Teacher() {
        // for JPA
    }

    public Teacher(String name, boolean accepted) {
        this.name = name;
        this.accepted = accepted;
    }

    /** Net reputation: (+1 per up-vote, -1 per down-vote), ignoring comment-only rows. */
    public int score() {
        return votes.stream()
                .filter(v -> v.getPositiveVote() != null)
                .mapToInt(v -> v.getPositiveVote() ? 1 : -1)
                .sum();
    }

    // --- Getters / setters ------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public List<TeacherAssignment> getAssignments() {
        return assignments;
    }

    public List<TeacherVote> getVotes() {
        return votes;
    }
}
