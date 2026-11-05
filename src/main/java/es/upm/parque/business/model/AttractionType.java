package es.upm.parque.business.model;

/**
 * Attraction types and the ticket access capability each one accepts.
 */
public enum AttractionType {

    /** Individual and group tickets. */
    GENERAL,
    /** Individual tickets only. */
    INDIVIDUAL,
    /** Group tickets only. */
    GROUP,
    /** Individual tickets with an unconsumed Special Pass. */
    SPECIAL;

    /** Creates an empty attraction of this type. */
    public Attraction<? extends GeneralAccess> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /** The type with exactly this name, or {@code null} when there is none. */
    public static AttractionType fromName(String name) {
        for (AttractionType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
