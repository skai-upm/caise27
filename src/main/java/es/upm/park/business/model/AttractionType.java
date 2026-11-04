package es.upm.park.business.model;

import java.util.Locale;

/** The kinds of attraction, each accepting a given kind of ticket. */
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

    /** Case-insensitive lookup; fails for unknown names. */
    public static AttractionType parse(String name) {
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown attraction type '" + name
                    + "', expected GENERAL, INDIVIDUAL, GROUP or SPECIAL");
        }
    }
}
