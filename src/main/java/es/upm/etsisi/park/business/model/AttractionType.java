package es.upm.etsisi.park.business.model;

import es.upm.etsisi.park.business.exception.ValidationException;
import java.util.Locale;

/**
 * Attraction kinds and the factory that creates the matching attraction class.
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

    /**
     * Parses a type name case-insensitively.
     *
     * @throws ValidationException if the name is not a known type
     */
    public static AttractionType fromName(String name) {
        String normalized = name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
        for (AttractionType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        throw new ValidationException("Invalid attraction type: " + name + ". Expected GENERAL, INDIVIDUAL, GROUP or SPECIAL");
    }
}
