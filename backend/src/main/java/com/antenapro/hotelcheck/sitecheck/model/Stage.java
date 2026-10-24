package com.antenapro.hotelcheck.sitecheck.model;

/**
 * Guest-journey stages (the vocabulary kept from the reference repo) with the plan's score weights:
 * Discover 30, Trust 25, Book 25, Understand 10, Explore 10.
 */
public enum Stage {
    DISCOVER("Discover", 30),
    UNDERSTAND("Understand", 10),
    EXPLORE("Explore", 10),
    TRUST("Trust", 25),
    BOOK("Book", 25);

    private final String label;
    private final int weight;

    Stage(String label, int weight) {
        this.label = label;
        this.weight = weight;
    }

    public String label() {
        return label;
    }

    public int weight() {
        return weight;
    }

    /** Journey order for the report: Discover, Understand, Explore, Trust, Book. */
    public static java.util.List<Stage> valuesInReportOrder() {
        return java.util.List.of(values());
    }
}
