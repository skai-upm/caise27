package es.upm.etsisi.amusementpark.business.model;

import java.util.Locale;
import java.util.Optional;

/** Kinds of attraction, each admitting a different kind of ticket. */
public enum AttractionType {
    /** Admits individual and group tickets. */
    GENERAL,
    /** Admits individual tickets only. */
    INDIVIDUAL,
    /** Admits group tickets only. */
    GROUP,
    /** Admits individual tickets only, with the additional Special Pass rules. */
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

    /** Parses a type name, ignoring case and surrounding blanks. */
    public static Optional<AttractionType> parse(String text) {
        String wanted = text == null ? "" : text.trim().toUpperCase(Locale.ROOT);
        for (AttractionType t : values()) {
            if (t.name().equals(wanted)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }
}
