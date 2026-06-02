package com.learnforlearning.repository;

import com.learnforlearning.domain.Grade;
import com.learnforlearning.domain.Subject;
import com.learnforlearning.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findByUserAndSubject(User user, Subject subject);

    Optional<Grade> findByUserAndSubject_Code(User user, String subjectCode);

    boolean existsByUserAndSubject(User user, Subject subject);
}
