package es.upm.parque.business.model;

import es.upm.parque.business.ParkException;
import java.util.Arrays;

/**
 * Kinds of attraction, by the tickets they admit.
 */
public enum AttractionType {
    /** Individual and group tickets. */
    GENERAL,
    /** Individual tickets only. */
    INDIVIDUAL,
    /** Group tickets only. */
    GROUP,
    /** Individual tickets, with Special Pass rules. */
    SPECIAL;

    /**
     * Creates an attraction of this type.
     */
    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /**
     * The type with that name (case-insensitive); other values are rejected.
     */
    public static AttractionType parse(String text) {
        return Arrays.stream(values()).filter(t -> t.name().equalsIgnoreCase(text.trim())).findFirst()
                .orElseThrow(() -> new ParkException("Invalid attraction type " + text + " (expected "
                        + String.join(", ", Arrays.stream(values()).map(Enum::name).toList()) + ")"));
    }
}
