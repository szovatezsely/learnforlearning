package com.learnforlearning.recommendation;

/**
 * Per-subject confusion counts accumulated during one boosting round, plus the
 * total weight of the samples the candidate stump misclassified.
 *
 * <p>The "good/bad selection" split is the two children of a decision stump: a
 * student is correctly classified when a <em>good selector</em> scored at or above
 * their own average in the subject, or a <em>bad selector</em> scored below it.
 */
final class SubjectStats {

    int correctOnGood;
    int incorrectOnGood;
    int correctOnBad;
    int incorrectOnBad;
    /** Sum of the weights of the samples this subject got wrong this round. */
    double misclassifiedWeight;

    boolean wasTouched() {
        return correctOnGood > 0 || incorrectOnGood > 0 || correctOnBad > 0 || incorrectOnBad > 0;
    }

    /** How many samples landed on this subject this round — used as the tie-breaker. */
    int occurrence() {
        return correctOnGood + incorrectOnGood + correctOnBad + incorrectOnBad;
    }

    /**
     * Weighted Gini impurity of the split: each branch's impurity weighted by the
     * share of samples it holds. Lower is better.
     */
    double giniImpurity() {
        int totalGood = correctOnGood + incorrectOnGood;
        int totalBad = correctOnBad + incorrectOnBad;
        int total = totalGood + totalBad;

        double leftGini = 1.0 - squaredShare(correctOnGood, totalGood) - squaredShare(incorrectOnGood, totalGood);
        double rightGini = 1.0 - squaredShare(correctOnBad, totalBad) - squaredShare(incorrectOnBad, totalBad);

        return ((double) totalGood / total) * leftGini + ((double) totalBad / total) * rightGini;
    }

    private static double squaredShare(int count, int total) {
        if (count <= 0 || total <= 0) {
            return 0.0;
        }
        double share = count / (double) total;
        return share * share;
    }
}
