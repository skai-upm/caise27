package es.upm.amusementpark.business.model;

/**
 * Kinds of attraction, each restricting the tickets it accepts.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /** Creates an attraction of this type. */
    public Attraction<?> create(String identifier, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(identifier, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(identifier, name, capacity);
            case GROUP -> new GroupAttraction(identifier, name, capacity);
            case SPECIAL -> new SpecialAttraction(identifier, name, capacity);
        };
    }

    /** The type with exactly the given name, or {@code null}. */
    public static AttractionType fromName(String name) {
        for (AttractionType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
