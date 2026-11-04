package es.upm.etsisi.amusementpark.business.model;

import java.util.Locale;

/** Kinds of attraction, by the tickets they admit. */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /** Creates an empty attraction of this type. */
    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /** Case-insensitive parse; unknown names are rejected. */
    public static AttractionType parse(String text) throws UserException {
        String name = text == null ? "" : text.trim().toUpperCase(Locale.ROOT);
        for (AttractionType t : values()) {
            if (t.name().equals(name)) {
                return t;
            }
        }
        throw new UserException("Unknown attraction type: " + text + " (use GENERAL, INDIVIDUAL, GROUP or SPECIAL)");
    }
}
