package com.learnforlearning.domain;

/**
 * A student's degree specialization track.
 *
 * <p>Modelled as an enum (rather than a free-form string) so the rest of the code can
 * branch on it type-safely. {@link #NONE} means "no specialization chosen yet".
 */
public enum Specialization {
    A,
    B,
    C,
    /** No specialization chosen yet. */
    NONE;

    public boolean isChosen() {
        return this != NONE;
    }
}
