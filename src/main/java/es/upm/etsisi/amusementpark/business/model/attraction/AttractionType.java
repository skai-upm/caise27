package es.upm.etsisi.amusementpark.business.model.attraction;

import es.upm.etsisi.amusementpark.business.exception.ValidationException;

/**
 * Attraction types and the attraction class each one creates.
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

    public static AttractionType parse(String text) throws ValidationException {
        for (AttractionType type : values()) {
            if (type.name().equals(text)) {
                return type;
            }
        }
        throw new ValidationException("unknown attraction type: " + text);
    }
}
