package com.learnforlearning.recommendation;

/**
 * A one-level decision tree produced in a boosting round: it splits on a single
 * subject and is trusted in proportion to its {@code amountOfSay}.
 *
 * @param subjectCode the subject this stump decides on
 * @param amountOfSay the stump's weight in the final vote, {@code 0.5 * ln((1-err)/err)}
 */
public record DecisionStump(String subjectCode, double amountOfSay) {
}
