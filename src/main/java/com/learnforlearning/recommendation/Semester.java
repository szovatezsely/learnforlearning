package com.learnforlearning.recommendation;

/**
 * Which half of the academic year a subject is offered in.
 *
 * <p>The submission form posts {@code "1"} (autumn) or {@code "2"} (spring); modelled
 * here as an enum.
 */
public enum Semester {
    /** Autumn / odd semester (form value {@code "1"}). */
    AUTUMN(false),
    /** Spring / even semester (form value {@code "2"}). */
    SPRING(true);

    private final boolean even;

    Semester(boolean even) {
        this.even = even;
    }

    public boolean isEven() {
        return even;
    }

    public static Semester fromFormValue(String value) {
        return switch (value) {
            case "1" -> AUTUMN;
            case "2" -> SPRING;
            default -> throw new IllegalArgumentException("Unknown semester value: " + value);
        };
    }
}
