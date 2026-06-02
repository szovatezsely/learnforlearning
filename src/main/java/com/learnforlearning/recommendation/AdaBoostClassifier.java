package com.learnforlearning.recommendation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * A self-written AdaBoost that recommends electives, organised into small, testable
 * pieces.
 *
 * <p>It builds up to {@code maxRounds} decision stumps. Each round it scores every
 * candidate subject by the (weighted) Gini impurity of splitting students into
 * "good selector" / "bad selector" on it, takes the lowest-impurity subject as the
 * round's stump, turns its error into an {@code amountOfSay}, reweights the samples,
 * and draws a fresh weighted-with-replacement resample for the next round.
 *
 * <h2>Deliberate design notes (deviations from textbook AdaBoost)</h2>
 * <ul>
 *   <li><b>Weights reset every round.</b> Boosting "memory" is carried by the
 *       resampling step, not by accumulating weights across rounds.</li>
 *   <li><b>Error term.</b> The round error is computed as
 *       {@code (#misclassified) * (sum of misclassified weights)} rather than just the
 *       sum of misclassified weights. That squares the error; it is an intentional
 *       characteristic of this recommender. See {@link #roundError}.</li>
 * </ul>
 * Randomness is injected so the resampling is reproducible in tests.
 */
@Component
public class AdaBoostClassifier {

    /** Default maximum number of boosting rounds — "results are usually the same after 6". */
    public static final int DEFAULT_MAX_ROUNDS = 6;

    private final int maxRounds;
    private final Random random;

    public AdaBoostClassifier() {
        this(DEFAULT_MAX_ROUNDS, new Random());
    }

    public AdaBoostClassifier(int maxRounds, Random random) {
        this.maxRounds = maxRounds;
        this.random = random;
    }

    /**
     * Trains the ensemble.
     *
     * @param samples        one entry per (student, taken-elective); mutated in place
     *                       (weights), and the list is replaced by resamples each round
     * @param candidateCodes the subjects eligible to become stumps (the logged-in
     *                       student's not-yet-taken electives for the chosen semester)
     * @return the ordered list of stumps (may be shorter than {@code maxRounds})
     */
    public List<DecisionStump> train(List<TrainingSample> samples, Set<String> candidateCodes) {
        List<DecisionStump> stumps = new ArrayList<>();
        if (samples.isEmpty() || candidateCodes.isEmpty()) {
            return stumps;
        }

        int sampleCount = samples.size();
        List<TrainingSample> current = new ArrayList<>(samples);
        boolean reachedErrorLimit = false;

        while (stumps.size() < maxRounds && !reachedErrorLimit) {
            resetWeights(current, sampleCount);

            Map<String, SubjectStats> stats = classify(current, candidateCodes);

            String best = pickLowestImpurity(stats);
            if (best == null) {
                break;
            }

            double rawError = roundError(stats.get(best));
            // An error above 1 means the ensemble is exhausted: take this final stump, then stop.
            if (rawError > 1.0) {
                reachedErrorLimit = true;
            }
            double error = clampError(rawError, sampleCount);

            double amountOfSay = 0.5 * Math.log((1.0 - error) / error);
            stumps.add(new DecisionStump(best, amountOfSay));

            reweight(current, best, amountOfSay);
            current = resample(current, sampleCount);
        }

        return stumps;
    }

    private static void resetWeights(List<TrainingSample> samples, int sampleCount) {
        double uniform = 1.0 / sampleCount;
        for (TrainingSample s : samples) {
            s.setWeight(uniform);
        }
    }

    /** Tallies how each candidate subject would split the samples this round. */
    private static Map<String, SubjectStats> classify(List<TrainingSample> samples, Set<String> candidateCodes) {
        Map<String, SubjectStats> stats = new LinkedHashMap<>();
        for (String code : candidateCodes) {
            stats.put(code, new SubjectStats());
        }

        for (TrainingSample s : samples) {
            SubjectStats st = stats.get(s.subjectCode());
            if (st == null) {
                s.setCorrect(null); // sample's subject is not a candidate — ignored this round
                continue;
            }
            boolean atOrAboveAverage = s.grade() >= s.studentAverage();
            if (s.goodSelector()) {
                if (atOrAboveAverage) {
                    st.correctOnGood++;
                    s.setCorrect(true);
                } else {
                    st.incorrectOnGood++;
                    st.misclassifiedWeight += s.weight();
                    s.setCorrect(false);
                }
            } else {
                if (!atOrAboveAverage) {
                    st.correctOnBad++;
                    s.setCorrect(true);
                } else {
                    st.incorrectOnBad++;
                    st.misclassifiedWeight += s.weight();
                    s.setCorrect(false);
                }
            }
        }
        return stats;
    }

    /**
     * Picks the touched subject with the lowest Gini impurity; ties are broken in favour
     * of the subject that more samples landed on. Returns null if nothing was touched.
     */
    private static String pickLowestImpurity(Map<String, SubjectStats> stats) {
        String best = null;
        double bestGini = Double.NaN;
        int bestOccurrence = -1;

        for (Map.Entry<String, SubjectStats> entry : stats.entrySet()) {
            SubjectStats st = entry.getValue();
            if (!st.wasTouched()) {
                continue;
            }
            double gini = st.giniImpurity();
            int occurrence = st.occurrence();
            if (best == null || gini < bestGini || (gini == bestGini && occurrence > bestOccurrence)) {
                best = entry.getKey();
                bestGini = gini;
                bestOccurrence = occurrence;
            }
        }
        return best;
    }

    /**
     * Round-error formula: misclassified count times summed misclassified weight
     * (see the class-level design note).
     */
    private static double roundError(SubjectStats st) {
        return (st.incorrectOnGood + st.incorrectOnBad) * st.misclassifiedWeight;
    }

    /** Keeps the error strictly inside (0, 1) so {@code ln((1-err)/err)} stays finite. */
    private static double clampError(double error, int sampleCount) {
        double epsilon = 1.0 / (sampleCount * 10.0);
        if (error > 1.0) {
            return 1.0 - epsilon;
        }
        if (error == 0.0) {
            return epsilon;
        }
        if (error == 1.0) {
            return 1.0 - epsilon;
        }
        return error;
    }

    /** Down-weights samples the stump got right, up-weights the ones it got wrong, then normalises. */
    private static void reweight(List<TrainingSample> samples, String bestCode, double amountOfSay) {
        double sum = 0.0;
        for (TrainingSample s : samples) {
            if (s.subjectCode().equals(bestCode) && Boolean.TRUE.equals(s.correct())) {
                s.setWeight(s.weight() * Math.exp(-amountOfSay));
            } else if (s.subjectCode().equals(bestCode) && Boolean.FALSE.equals(s.correct())) {
                s.setWeight(s.weight() * Math.exp(amountOfSay));
            }
            sum += s.weight();
        }
        for (TrainingSample s : samples) {
            s.setWeight(s.weight() / sum);
        }
    }

    /** Weighted resampling with replacement: heavier (harder) samples appear more often. */
    private List<TrainingSample> resample(List<TrainingSample> samples, int sampleCount) {
        List<TrainingSample> resampled = new ArrayList<>(sampleCount);
        for (int i = 0; i < sampleCount; i++) {
            double target = random.nextDouble();
            double cumulative = 0.0;
            for (TrainingSample s : samples) {
                cumulative += s.weight();
                if (cumulative > target) {
                    resampled.add(s);
                    break;
                }
            }
        }
        return resampled;
    }
}
