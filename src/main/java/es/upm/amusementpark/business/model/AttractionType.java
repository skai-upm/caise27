package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.InvalidValueException;

/** Attraction types and the attraction class that implements each one. */
public enum AttractionType {
    GENERAL, INDIVIDUAL, GROUP, SPECIAL;

    /** @throws InvalidValueException for a name that is not a type */
    public static AttractionType parse(String text) {
        String value = text == null ? "" : text.trim();
        for (AttractionType type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        throw new InvalidValueException("unknown attraction type (expected GENERAL, INDIVIDUAL, GROUP or SPECIAL): "
                + text);
    }

    public Attraction<?> create(String identifier, String name, int capacity) {
        switch (this) {
            case INDIVIDUAL:
                return new IndividualAttraction(identifier, name, capacity);
            case GROUP:
                return new GroupAttraction(identifier, name, capacity);
            case SPECIAL:
                return new SpecialAttraction(identifier, name, capacity);
            default:
                return new GeneralAttraction(identifier, name, capacity);
        }
    }
}
