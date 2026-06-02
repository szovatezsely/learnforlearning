package com.learnforlearning.repository;

import com.learnforlearning.domain.Specialization;
import com.learnforlearning.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Loads every student who has chosen a specialization together with their grades
     * and the grades' subjects in a single query, so the AdaBoost loop does not fire a
     * query per student.
     */
    @Query("""
            select distinct u from User u
            left join fetch u.grades g
            left join fetch g.subject
            where u.specialization <> :none
            """)
    List<User> findAllWithGradesForTraining(Specialization none);

    default List<User> findAllWithGradesForTraining() {
        return findAllWithGradesForTraining(Specialization.NONE);
    }
}
