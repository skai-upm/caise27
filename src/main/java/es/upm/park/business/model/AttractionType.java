package es.upm.park.business.model;

import es.upm.park.business.exception.InvalidInputException;

/** The kinds of attraction, each restricting the tickets it accepts. */
public enum AttractionType {
    GENERAL, INDIVIDUAL, GROUP, SPECIAL;

    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    public static AttractionType parse(String text) {
        for (AttractionType type : values()) {
            if (type.name().equals(text)) {
                return type;
            }
        }
        throw new InvalidInputException("Unknown attraction type: " + text
                + " (expected GENERAL, INDIVIDUAL, GROUP or SPECIAL)");
    }
}
