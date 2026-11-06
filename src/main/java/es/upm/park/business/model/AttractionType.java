package es.upm.park.business.model;

/**
 * Attraction types, each restricting the tickets it accepts.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /** Creates an attraction of this type. */
    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /** The type with exactly this name, or {@code null}. */
    public static AttractionType fromName(String name) {
        for (AttractionType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
