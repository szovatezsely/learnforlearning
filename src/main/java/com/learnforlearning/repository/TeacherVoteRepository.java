package com.learnforlearning.repository;

import com.learnforlearning.domain.Teacher;
import com.learnforlearning.domain.TeacherVote;
import com.learnforlearning.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeacherVoteRepository extends JpaRepository<TeacherVote, Long> {

    Optional<TeacherVote> findByUserAndTeacher(User user, Teacher teacher);

    List<TeacherVote> findByTeacher(Teacher teacher);

    List<TeacherVote> findByUser(User user);

    /** A handful of the most recent non-empty comments for the landing page. */
    @Query("""
            select v from TeacherVote v
            join fetch v.user
            join fetch v.teacher
            where v.comment is not null and v.comment <> ''
            order by v.id desc
            """)
    List<TeacherVote> findRecentComments();
}
