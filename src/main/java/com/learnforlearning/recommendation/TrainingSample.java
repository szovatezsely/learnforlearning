package com.learnforlearning.recommendation;

/**
 * One (student, elective-subject) observation fed to the booster.
 *
 * <p>Everything the algorithm needs is pre-computed onto the sample so the training
 * loop touches no entities or database. {@link #weight} and {@link #correct} are the
 * only mutable parts — they change every boosting round.
 */
public final class TrainingSample {

    private final String subjectCode;
    private final int grade;
    /** The student's overall grade average — the decision boundary for this sample. */
    private final double studentAverage;
    /** Whether the student's electives improved their average (the class label). */
    private final boolean goodSelector;

    private double weight;
    /** Null until classified in a round; then whether the chosen stump got it right. */
    private Boolean correct;

    public TrainingSample(String subjectCode, int grade, double studentAverage, boolean goodSelector) {
        this.subjectCode = subjectCode;
        this.grade = grade;
        this.studentAverage = studentAverage;
        this.goodSelector = goodSelector;
    }

    public String subjectCode() {
        return subjectCode;
    }

    public int grade() {
        return grade;
    }

    public double studentAverage() {
        return studentAverage;
    }

    public boolean goodSelector() {
        return goodSelector;
    }

    public double weight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Boolean correct() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }
}
