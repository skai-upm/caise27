package com.amusementpark.business.model;

/**
 * Attraction types by the kind of ticket they admit.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /** Resolves a type name ignoring case and surrounding spaces, or {@code null} if unknown. */
    public static AttractionType fromText(String text) {
        String wanted = text.trim().toUpperCase();
        for (AttractionType t : values()) {
            if (t.name().equals(wanted)) {
                return t;
            }
        }
        return null;
    }
}
