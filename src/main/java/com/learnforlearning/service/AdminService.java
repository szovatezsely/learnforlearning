package com.learnforlearning.service;

import com.learnforlearning.domain.Subject;
import com.learnforlearning.domain.Teacher;
import com.learnforlearning.domain.TeacherAssignment;
import com.learnforlearning.repository.SubjectRepository;
import com.learnforlearning.repository.TeacherAssignmentRepository;
import com.learnforlearning.repository.TeacherRepository;
import com.learnforlearning.web.view.DisputeView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Moderation actions for admins: approving suggested teachers/subjects, resolving
 * activity disputes, and deletions.
 */
@Service
public class AdminService {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherAssignmentRepository assignmentRepository;

    public AdminService(TeacherRepository teacherRepository,
                        SubjectRepository subjectRepository,
                        TeacherAssignmentRepository assignmentRepository) {
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DisputeView> disputes() {
        return assignmentRepository.findDisputed().stream()
                .map(a -> new DisputeView(a.getTeacher(), a.getSubject().getId(),
                        a.getSubject().getName(), a.isActive(), a.getGoingAgainst()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Teacher> pendingTeachers() {
        return teacherRepository.findByAcceptedFalse();
    }

    @Transactional(readOnly = true)
    public List<Subject> pendingSubjects() {
        return subjectRepository.findByAcceptedFalse();
    }

    @Transactional
    public boolean setTeacherActivity(Long teacherId, Long subjectId, boolean active) {
        TeacherAssignment assignment = findAssignment(teacherId, subjectId);
        if (assignment == null) {
            return false;
        }
        assignment.setActive(active);
        return true;
    }

    @Transactional
    public boolean acceptTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        if (teacher == null) {
            return false;
        }
        teacher.setAccepted(true);
        assignmentRepository.findByTeacher(teacher).forEach(a -> a.setActive(true));
        return true;
    }

    @Transactional
    public boolean acceptSubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject == null) {
            return false;
        }
        subject.setAccepted(true);
        return true;
    }

    @Transactional
    public boolean resetDispute(Long teacherId, Long subjectId) {
        TeacherAssignment assignment = findAssignment(teacherId, subjectId);
        if (assignment == null) {
            return false;
        }
        assignment.resetGoingAgainst();
        return true;
    }

    @Transactional
    public boolean deleteTeacher(Long teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            return false;
        }
        teacherRepository.deleteById(teacherId);
        return true;
    }

    @Transactional
    public boolean deleteSubject(Long subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            return false;
        }
        subjectRepository.deleteById(subjectId);
        return true;
    }

    private TeacherAssignment findAssignment(Long teacherId, Long subjectId) {
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (teacher == null || subject == null) {
            return null;
        }
        return assignmentRepository.findBySubjectAndTeacher(subject, teacher).orElse(null);
    }
}
