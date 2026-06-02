package com.learnforlearning.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * A student (or admin) of the platform.
 *
 * <p>Domain queries that only need the already-loaded {@link #grades} collection
 * live here as pure methods (no DB access), so the recommendation engine can call
 * them after a single fetch. Anything that needs the database lives in the services.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Specialization specialization = Specialization.NONE;

    @Column(nullable = false)
    private boolean admin = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Grade> grades = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeacherVote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Calculation> calculations = new ArrayList<>();

    protected User() {
        // for JPA
    }

    public User(String name, String email, String password, Specialization specialization, boolean admin) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.specialization = specialization;
        this.admin = admin;
    }

    // --- Pure domain logic over the loaded grade collection ---------------------

    public boolean hasSpecialization() {
        return specialization.isChosen();
    }

    /** Average over all recorded grades, or empty if none. */
    public OptionalDouble averageGrade() {
        return grades.stream().mapToInt(Grade::getValue).average();
    }

    /** The grades the student earned in subjects that are electives on their track. */
    public List<Grade> optionalGrades() {
        if (!hasSpecialization()) {
            return List.of();
        }
        return grades.stream()
                .filter(g -> g.getSubject().isOptionalOn(specialization))
                .toList();
    }

    /** Average grade restricted to elective subjects, or empty if the student has none. */
    public OptionalDouble averageOptionalGrade() {
        return optionalGrades().stream().mapToInt(Grade::getValue).average();
    }

    public boolean hasGradeFor(Subject subject) {
        return grades.stream().anyMatch(g -> g.getSubject().equals(subject));
    }

    // --- Getters / setters ------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<Grade> getGrades() {
        return grades;
    }

    public List<TeacherVote> getVotes() {
        return votes;
    }

    public List<Calculation> getCalculations() {
        return calculations;
    }
}
