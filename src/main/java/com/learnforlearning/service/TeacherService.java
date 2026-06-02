package com.learnforlearning.service;

import com.learnforlearning.domain.Subject;
import com.learnforlearning.domain.Teacher;
import com.learnforlearning.domain.TeacherAssignment;
import com.learnforlearning.domain.TeacherVote;
import com.learnforlearning.repository.SubjectRepository;
import com.learnforlearning.repository.TeacherAssignmentRepository;
import com.learnforlearning.repository.TeacherRepository;
import com.learnforlearning.repository.TeacherVoteRepository;
import com.learnforlearning.web.view.CommentView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherAssignmentRepository assignmentRepository;
    private final TeacherVoteRepository voteRepository;

    public TeacherService(TeacherRepository teacherRepository,
                          SubjectRepository subjectRepository,
                          TeacherAssignmentRepository assignmentRepository,
                          TeacherVoteRepository voteRepository) {
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.assignmentRepository = assignmentRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Teacher> findById(Long id) {
        return teacherRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<TeacherAssignment> activeAssignmentsFor(Subject subject) {
        return assignmentRepository.findBySubject(subject).stream()
                .filter(TeacherAssignment::isActive)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentView> commentsOf(Teacher teacher) {
        return voteRepository.findByTeacher(teacher).stream()
                .map(v -> new CommentView(v.getUser().getName(), v.getComment(), v.getPositiveVote()))
                .toList();
    }

    /** Suggests a new (unaccepted) teacher and links them to a subject as inactive. */
    @Transactional
    public void suggestTeacher(String name, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("No such subject: " + subjectId));
        Teacher teacher = teacherRepository.save(new Teacher(name, false));
        assignmentRepository.save(new TeacherAssignment(subject, teacher, false));
    }

    /**
     * Records a student disputing a teacher's activity on a subject: if the claimed
     * state differs from the current one, the dispute counter is bumped.
     */
    @Transactional
    public boolean dispute(Long teacherId, Long subjectId, boolean claimedActive) {
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (teacher == null || subject == null) {
            return false;
        }
        TeacherAssignment assignment = assignmentRepository.findBySubjectAndTeacher(subject, teacher).orElse(null);
        if (assignment == null) {
            return false;
        }
        if (assignment.isActive() != claimedActive) {
            assignment.incrementGoingAgainst();
        }
        return true;
    }
}
