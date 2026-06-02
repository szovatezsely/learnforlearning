package com.learnforlearning.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AdaBoostClassifierTest {

    private final AdaBoostClassifier classifier = new AdaBoostClassifier(6, new Random(42));

    @Test
    void noSamplesProducesNoStumps() {
        assertThat(classifier.train(List.of(), Set.of("X"))).isEmpty();
    }

    @Test
    void noCandidatesProducesNoStumps() {
        List<TrainingSample> samples = List.of(new TrainingSample("X", 5, 3.0, true));
        assertThat(classifier.train(samples, Set.of())).isEmpty();
    }

    @Test
    void producesAtMostMaxRounds() {
        List<TrainingSample> samples = mixedSamples();
        List<DecisionStump> stumps = new AdaBoostClassifier(3, new Random(1)).train(samples, Set.of("X", "Y"));
        assertThat(stumps).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void perfectlySeparatingSubjectIsChosenFirstWithPositiveSay() {
        List<TrainingSample> samples = new ArrayList<>();
        // Subject X separates perfectly: good selectors are at/above average, bad ones below.
        samples.add(new TrainingSample("X", 5, 3.0, true));
        samples.add(new TrainingSample("X", 4, 3.0, true));
        samples.add(new TrainingSample("X", 2, 3.0, false));
        samples.add(new TrainingSample("X", 1, 3.0, false));
        // Subject Y is pure noise: every good selector is misclassified.
        samples.add(new TrainingSample("Y", 1, 3.0, true));
        samples.add(new TrainingSample("Y", 2, 3.0, true));

        List<DecisionStump> stumps = classifier.train(samples, Set.of("X", "Y"));

        assertThat(stumps).isNotEmpty();
        assertThat(stumps.get(0).subjectCode()).isEqualTo("X");
        assertThat(stumps.get(0).amountOfSay()).isPositive();
    }

    private static List<TrainingSample> mixedSamples() {
        List<TrainingSample> samples = new ArrayList<>();
        samples.add(new TrainingSample("X", 5, 3.0, true));
        samples.add(new TrainingSample("X", 1, 3.0, false));
        samples.add(new TrainingSample("Y", 4, 3.0, true));
        samples.add(new TrainingSample("Y", 5, 3.0, false));
        return samples;
    }

    @Test
    void giniImpurityIsZeroForAPureSplitAndHalfForAnEvenOne() {
        SubjectStats pure = new SubjectStats();
        pure.correctOnGood = 4;
        pure.correctOnBad = 4;
        assertThat(pure.giniImpurity()).isCloseTo(0.0, within(1e-9));

        SubjectStats even = new SubjectStats();
        even.correctOnGood = 2;
        even.incorrectOnGood = 2;
        even.correctOnBad = 2;
        even.incorrectOnBad = 2;
        assertThat(even.giniImpurity()).isCloseTo(0.5, within(1e-9));
    }
}
