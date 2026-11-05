package es.upm.park.business.model;

import java.util.Optional;

/**
 * Kinds of attraction, each admitting a specific class of ticket.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /** New, empty attraction of this type. */
    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /** Type with exactly this (uppercase) name. */
    public static Optional<AttractionType> parse(String text) {
        for (AttractionType type : values()) {
            if (type.name().equals(text)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
