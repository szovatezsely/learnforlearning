package com.learnforlearning.service;

import com.learnforlearning.domain.Grade;
import com.learnforlearning.domain.Specialization;
import com.learnforlearning.domain.Subject;
import com.learnforlearning.domain.User;
import com.learnforlearning.repository.GradeRepository;
import com.learnforlearning.repository.SubjectRepository;
import com.learnforlearning.web.dto.SubjectSuggestionForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;

    public SubjectService(SubjectRepository subjectRepository, GradeRepository gradeRepository) {
        this.subjectRepository = subjectRepository;
        this.gradeRepository = gradeRepository;
    }

    @Transactional(readOnly = true)
    public List<Subject> listAccepted() {
        return subjectRepository.findByAcceptedTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Subject> findById(Long id) {
        return subjectRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Subject> findAccepted(Long id) {
        return subjectRepository.findById(id).filter(Subject::isAccepted);
    }

    @Transactional(readOnly = true)
    public List<Subject> search(String text) {
        if (text == null || text.isBlank()) {
            return subjectRepository.findAll();
        }
        return subjectRepository.findByNameContainingIgnoreCase(text.trim());
    }

    /**
     * Subjects the student can still add a grade for: those offered on their track
     * (everything, if no track chosen) that they have not graded yet.
     */
    @Transactional(readOnly = true)
    public List<Subject> gradableSubjectsFor(User user) {
        Set<String> takenCodes = user.getGrades().stream()
                .map(g -> g.getSubject().getCode())
                .collect(Collectors.toSet());
        Specialization spec = user.getSpecialization();
        return subjectRepository.findAll().stream()
                .filter(s -> s.existsOn(spec))
                .filter(s -> !takenCodes.contains(s.getCode()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Grade> gradesOf(User user) {
        return user.getGrades();
    }

    /** Accepted electives on the student's track that they have not taken yet (any semester). */
    @Transactional(readOnly = true)
    public List<Subject> availableOptionalSubjectsFor(User user) {
        if (!user.hasSpecialization()) {
            return List.of();
        }
        Set<String> takenCodes = user.getGrades().stream()
                .map(g -> g.getSubject().getCode())
                .collect(Collectors.toSet());
        Specialization spec = user.getSpecialization();
        return subjectRepository.findByAcceptedTrue().stream()
                .filter(s -> s.isOptionalOn(spec))
                .filter(s -> !takenCodes.contains(s.getCode()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Subject> findByCode(String code) {
        return subjectRepository.findByCode(code);
    }

    @Transactional
    public Subject suggestSubject(SubjectSuggestionForm form) {
        Subject subject = new Subject(
                form.getName(), form.getCode(), form.getCredit(), form.isEvenSemester(),
                form.isExistsOnA(), form.isExistsOnB(), form.isExistsOnC(),
                form.isOptionalOnA(), form.isOptionalOnB(), form.isOptionalOnC(),
                form.getUrl(), false);
        return subjectRepository.save(subject);
    }
}
