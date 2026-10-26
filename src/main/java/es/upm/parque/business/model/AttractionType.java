package es.upm.parque.business.model;

import java.util.Locale;

/**
 * Kinds of attraction, by the tickets they admit.
 */
public enum AttractionType {
    GENERAL, INDIVIDUAL, GROUP, SPECIAL;

    /**
     * Returns the type with this name ignoring case, or null when there is none.
     */
    public static AttractionType parse(String text) {
        for (AttractionType type : values()) {
            if (type.name().equals(text.trim().toUpperCase(Locale.ROOT))) {
                return type;
            }
        }
        return null;
    }

    /**
     * Creates an attraction of this type.
     */
    public Attraction<? extends Admission> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }
}
