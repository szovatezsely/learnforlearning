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
 * A university course/subject.
 *
 * <p>Whether the subject exists on, and is an elective on, each track (A/B/C) is
 * kept as a pair of boolean trios, but the lookups are funnelled through
 * {@link #existsOn(Specialization)} and
 * {@link #isOptionalOn(Specialization)} so callers never branch on the track by hand.
 */
@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private int creditPoints;

    /** True if the subject runs in the (spring) even semester, false for the odd one. */
    @Column(nullable = false)
    private boolean evenSemester;

    @Column(nullable = false)
    private boolean existsOnA;
    @Column(nullable = false)
    private boolean existsOnB;
    @Column(nullable = false)
    private boolean existsOnC;

    @Column(nullable = false)
    private boolean optionalOnA;
    @Column(nullable = false)
    private boolean optionalOnB;
    @Column(nullable = false)
    private boolean optionalOnC;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private boolean accepted;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherAssignment> teacherAssignments = new ArrayList<>();

    protected Subject() {
        // for JPA
    }

    public Subject(String name, String code, int creditPoints, boolean evenSemester,
                   boolean existsOnA, boolean existsOnB, boolean existsOnC,
                   boolean optionalOnA, boolean optionalOnB, boolean optionalOnC,
                   String url, boolean accepted) {
        this.name = name;
        this.code = code;
        this.creditPoints = creditPoints;
        this.evenSemester = evenSemester;
        this.existsOnA = existsOnA;
        this.existsOnB = existsOnB;
        this.existsOnC = existsOnC;
        this.optionalOnA = optionalOnA;
        this.optionalOnB = optionalOnB;
        this.optionalOnC = optionalOnC;
        this.url = url;
        this.accepted = accepted;
    }

    public boolean existsOn(Specialization spec) {
        return switch (spec) {
            case A -> existsOnA;
            case B -> existsOnB;
            case C -> existsOnC;
            case NONE -> true; // a student with no track sees everything
        };
    }

    public boolean isOptionalOn(Specialization spec) {
        return switch (spec) {
            case A -> existsOnA && optionalOnA;
            case B -> existsOnB && optionalOnB;
            case C -> existsOnC && optionalOnC;
            case NONE -> false;
        };
    }

    // --- Getters / setters ------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getCreditPoints() {
        return creditPoints;
    }

    public boolean isEvenSemester() {
        return evenSemester;
    }

    public boolean isExistsOnA() {
        return existsOnA;
    }

    public boolean isExistsOnB() {
        return existsOnB;
    }

    public boolean isExistsOnC() {
        return existsOnC;
    }

    public boolean isOptionalOnA() {
        return optionalOnA;
    }

    public boolean isOptionalOnB() {
        return optionalOnB;
    }

    public boolean isOptionalOnC() {
        return optionalOnC;
    }

    public String getUrl() {
        return url;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public List<TeacherAssignment> getTeacherAssignments() {
        return teacherAssignments;
    }
}
