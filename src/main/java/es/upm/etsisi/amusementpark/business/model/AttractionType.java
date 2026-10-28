package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;

/**
 * Attraction types, each restricting the tickets accepted for entry.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /**
     * @throws InvalidValueException if {@code text} is not one of the type names
     */
    public static AttractionType parse(String text) {
        for (AttractionType type : values()) {
            if (type.name().equals(text)) {
                return type;
            }
        }
        throw new InvalidValueException("Invalid attraction type, expected GENERAL, INDIVIDUAL, GROUP or SPECIAL: "
                + text);
    }

    /** Creates an empty attraction of this type. */
    public Attraction<?> create(String identifier, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(identifier, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(identifier, name, capacity);
            case GROUP -> new GroupAttraction(identifier, name, capacity);
            case SPECIAL -> new SpecialAttraction(identifier, name, capacity);
        };
    }
}
