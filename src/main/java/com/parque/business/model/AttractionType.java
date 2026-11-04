package com.parque.business.model;

/**
 * Attraction types according to the tickets they admit.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /** Creates an attraction of this type. */
    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /** The type with exactly this name, or an {@link IllegalArgumentException}. */
    public static AttractionType parse(String name) {
        for (AttractionType t : values()) {
            if (t.name().equals(name)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown attraction type: '" + name
                + "' (expected GENERAL, INDIVIDUAL, GROUP or SPECIAL)");
    }
}
