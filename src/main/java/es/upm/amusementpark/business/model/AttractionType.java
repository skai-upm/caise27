package es.upm.amusementpark.business.model;

import java.util.Optional;

/**
 * Attraction types and the factory of the corresponding attraction.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    public Attraction<?> create(String identifier, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(identifier, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(identifier, name, capacity);
            case GROUP -> new GroupAttraction(identifier, name, capacity);
            case SPECIAL -> new SpecialAttraction(identifier, name, capacity);
        };
    }

    public static Optional<AttractionType> fromName(String name) {
        for (AttractionType t : values()) {
            if (t.name().equals(name)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }
}
