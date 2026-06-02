package com.learnforlearning.repository;

import com.learnforlearning.domain.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByCode(String code);

    List<Subject> findByAcceptedTrue();

    List<Subject> findByAcceptedFalse();

    List<Subject> findByNameContainingIgnoreCase(String fragment);
}
