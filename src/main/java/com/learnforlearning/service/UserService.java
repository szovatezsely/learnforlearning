package com.learnforlearning.service;

import com.learnforlearning.domain.Grade;
import com.learnforlearning.domain.Specialization;
import com.learnforlearning.domain.Subject;
import com.learnforlearning.domain.Teacher;
import com.learnforlearning.domain.TeacherVote;
import com.learnforlearning.domain.User;
import com.learnforlearning.repository.CalculationRepository;
import com.learnforlearning.repository.GradeRepository;
import com.learnforlearning.repository.SubjectRepository;
import com.learnforlearning.repository.TeacherRepository;
import com.learnforlearning.repository.TeacherVoteRepository;
import com.learnforlearning.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Student-facing operations: registration, grade book, teacher votes/comments,
 * specialization and calculation history.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final GradeRepository gradeRepository;
    private final TeacherVoteRepository voteRepository;
    private final CalculationRepository calculationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       SubjectRepository subjectRepository,
                       TeacherRepository teacherRepository,
                       GradeRepository gradeRepository,
                       TeacherVoteRepository voteRepository,
                       CalculationRepository calculationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.gradeRepository = gradeRepository;
        this.voteRepository = voteRepository;
        this.calculationRepository = calculationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Accounts ---------------------------------------------------------------

    @Transactional
    public User register(String name, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ezzel az e-mail címmel már regisztráltak.");
        }
        User user = new User(name, email, passwordEncoder.encode(rawPassword), Specialization.NONE, false);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such user: " + id));
    }

    @Transactional
    public void updateSpecialization(Long userId, Specialization specialization) {
        getById(userId).setSpecialization(specialization);
    }

    // --- Grade book -------------------------------------------------------------

    /** Adds a grade only if the student does not already have one for the subject. */
    @Transactional
    public boolean addGrade(Long userId, Long subjectId, int value) {
        User user = getById(userId);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("No such subject: " + subjectId));
        if (gradeRepository.existsByUserAndSubject(user, subject)) {
            return false;
        }
        gradeRepository.save(new Grade(user, subject, value));
        return true;
    }

    @Transactional
    public void updateGrade(Long userId, Long subjectId, int value) {
        User user = getById(userId);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("No such subject: " + subjectId));
        Grade grade = gradeRepository.findByUserAndSubject(user, subject)
                .orElseGet(() -> new Grade(user, subject, value));
        grade.setValue(value);
        gradeRepository.save(grade);
    }

    @Transactional
    public boolean deleteGrade(Long userId, Long subjectId) {
        User user = getById(userId);
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject == null) {
            return false;
        }
        Optional<Grade> grade = gradeRepository.findByUserAndSubject(user, subject);
        grade.ifPresent(gradeRepository::delete);
        return grade.isPresent();
    }

    @Transactional(readOnly = true)
    public Optional<Grade> findGrade(Long userId, Long subjectId) {
        User user = getById(userId);
        return subjectRepository.findById(subjectId)
                .flatMap(subject -> gradeRepository.findByUserAndSubject(user, subject));
    }

    // --- Teacher votes & comments ----------------------------------------------

    /**
     * Toggles a vote: clicking the same direction again clears it (keeping any comment),
     * a fresh or opposite click sets it.
     */
    @Transactional
    public void vote(Long userId, Long teacherId, boolean positive) {
        User user = getById(userId);
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("No such teacher: " + teacherId));

        TeacherVote vote = voteRepository.findByUserAndTeacher(user, teacher).orElse(null);
        if (vote != null && Boolean.valueOf(positive).equals(vote.getPositiveVote())) {
            if (vote.getComment() != null && !vote.getComment().isBlank()) {
                vote.setPositiveVote(null);
            } else {
                voteRepository.delete(vote);
            }
            return;
        }
        if (vote == null) {
            vote = new TeacherVote(teacher, user);
        }
        vote.setPositiveVote(positive);
        voteRepository.save(vote);
    }

    @Transactional
    public void addComment(Long userId, Long teacherId, String comment) {
        User user = getById(userId);
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("No such teacher: " + teacherId));
        TeacherVote vote = voteRepository.findByUserAndTeacher(user, teacher)
                .orElseGet(() -> new TeacherVote(teacher, user));
        vote.setComment(comment);
        voteRepository.save(vote);
    }

    @Transactional
    public boolean deleteComment(Long userId, Long teacherId) {
        User user = getById(userId);
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        if (teacher == null) {
            return false;
        }
        TeacherVote vote = voteRepository.findByUserAndTeacher(user, teacher).orElse(null);
        if (vote == null) {
            return false;
        }
        if (vote.getPositiveVote() != null) {
            vote.setComment(null);
        } else {
            voteRepository.delete(vote);
        }
        return true;
    }

    @Transactional(readOnly = true)
    public Optional<TeacherVote> findVote(Long userId, Long teacherId) {
        User user = getById(userId);
        return teacherRepository.findById(teacherId)
                .flatMap(teacher -> voteRepository.findByUserAndTeacher(user, teacher));
    }

    @Transactional(readOnly = true)
    public List<TeacherVote> votesOf(Long userId) {
        return voteRepository.findByUser(getById(userId));
    }

    // --- Calculation history ----------------------------------------------------

    @Transactional
    public void deleteCalculations(Long userId) {
        calculationRepository.deleteByUser(getById(userId));
    }
}
