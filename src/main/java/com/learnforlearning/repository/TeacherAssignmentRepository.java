package com.learnforlearning.repository;

import com.learnforlearning.domain.Subject;
import com.learnforlearning.domain.Teacher;
import com.learnforlearning.domain.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {

    Optional<TeacherAssignment> findBySubjectAndTeacher(Subject subject, Teacher teacher);

    List<TeacherAssignment> findBySubject(Subject subject);

    List<TeacherAssignment> findByTeacher(Teacher teacher);

    /** Active assignments of accepted teachers that have at least one pending dispute. */
    @Query("""
            select a from TeacherAssignment a
            join fetch a.teacher t
            join fetch a.subject s
            where t.accepted = true and a.goingAgainst > 0
            """)
    List<TeacherAssignment> findDisputed();
}
