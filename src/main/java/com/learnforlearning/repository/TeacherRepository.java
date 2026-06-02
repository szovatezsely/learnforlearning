package com.learnforlearning.repository;

import com.learnforlearning.domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByName(String name);

    List<Teacher> findByAcceptedTrue();

    List<Teacher> findByAcceptedFalse();
}
