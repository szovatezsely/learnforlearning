package com.learnforlearning.recommendation;

import com.learnforlearning.domain.Calculation;
import com.learnforlearning.domain.Grade;
import com.learnforlearning.domain.Subject;
import com.learnforlearning.domain.User;
import com.learnforlearning.repository.CalculationRepository;
import com.learnforlearning.repository.SubjectRepository;
import com.learnforlearning.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Turns the booster into an end-to-end recommendation: it assembles the training
 * data, picks the candidate subjects for the requesting student, runs
 * {@link AdaBoostClassifier}, scores the resulting stumps, and records the result.
 */
@Service
public class RecommendationService {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final CalculationRepository calculationRepository;
    private final AdaBoostClassifier classifier;

    public RecommendationService(UserRepository userRepository,
                                 SubjectRepository subjectRepository,
                                 CalculationRepository calculationRepository,
                                 AdaBoostClassifier classifier) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.calculationRepository = calculationRepository;
        this.classifier = classifier;
    }

    /**
     * Recommends one elective for the given student and semester, and persists it to
     * their calculation history when successful.
     */
    @Transactional
    public RecommendationResult recommend(Long userId, Semester semester) {
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No such user: " + userId));

        if (!student.hasSpecialization()) {
            return RecommendationResult.failure();
        }

        Set<String> candidateCodes = candidateCodesFor(student, semester);
        if (candidateCodes.isEmpty()) {
            return RecommendationResult.failure();
        }

        List<User> trainingUsers = userRepository.findAllWithGradesForTraining();
        List<TrainingSample> samples = buildSamples(trainingUsers);

        List<DecisionStump> stumps = classifier.train(samples, candidateCodes);
        if (stumps.isEmpty()) {
            return RecommendationResult.failure();
        }

        String bestCode = scoreStumps(stumps, trainingUsers);
        if (bestCode == null) {
            return RecommendationResult.failure();
        }

        Subject recommended = subjectRepository.findByCode(bestCode).orElse(null);
        if (recommended == null) {
            return RecommendationResult.failure();
        }

        calculationRepository.save(new Calculation(student, recommended.getCode()));
        return RecommendationResult.success(recommended);
    }

    /** The student's accepted, not-yet-taken electives offered in the chosen semester. */
    private Set<String> candidateCodesFor(User student, Semester semester) {
        Set<String> taken = student.getGrades().stream()
                .map(g -> g.getSubject().getCode())
                .collect(Collectors.toSet());

        return subjectRepository.findByAcceptedTrue().stream()
                .filter(s -> s.isOptionalOn(student.getSpecialization()))
                .filter(s -> !taken.contains(s.getCode()))
                .filter(s -> s.isEvenSemester() == semester.isEven())
                .map(Subject::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** One sample per elective grade across all specialised students. */
    private List<TrainingSample> buildSamples(List<User> trainingUsers) {
        List<TrainingSample> samples = new java.util.ArrayList<>();
        for (User user : trainingUsers) {
            List<Grade> optionalGrades = user.optionalGrades();
            if (optionalGrades.isEmpty()) {
                continue;
            }
            double average = user.averageGrade().orElseThrow();
            double optionalAverage = user.averageOptionalGrade().orElseThrow();
            // "Good selector": their electives are at least as good as their overall average.
            boolean goodSelector = average <= optionalAverage;
            for (Grade g : optionalGrades) {
                samples.add(new TrainingSample(g.getSubject().getCode(), g.getValue(), average, goodSelector));
            }
        }
        return samples;
    }

    /**
     * Final vote: each student's grade in a stump subject adds the stump's say if they
     * scored at/above their average and subtracts it otherwise. Highest total wins.
     */
    private String scoreStumps(List<DecisionStump> stumps, List<User> trainingUsers) {
        Map<String, Double> scores = new LinkedHashMap<>();
        for (DecisionStump stump : stumps) {
            scores.putIfAbsent(stump.subjectCode(), 0.0);
        }

        for (User user : trainingUsers) {
            OptionalDouble average = user.averageGrade();
            if (average.isEmpty()) {
                continue;
            }
            double avg = average.getAsDouble();
            for (Grade grade : user.optionalGrades()) {
                String code = grade.getSubject().getCode();
                if (!scores.containsKey(code)) {
                    continue;
                }
                for (DecisionStump stump : stumps) {
                    if (stump.subjectCode().equals(code)) {
                        double delta = grade.getValue() >= avg ? stump.amountOfSay() : -stump.amountOfSay();
                        scores.merge(code, delta, Double::sum);
                    }
                }
            }
        }

        String best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            if (best == null || entry.getValue() > bestScore) {
                best = entry.getKey();
                bestScore = entry.getValue();
            }
        }
        return best;
    }
}
