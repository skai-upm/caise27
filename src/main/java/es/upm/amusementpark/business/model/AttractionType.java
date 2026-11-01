package es.upm.amusementpark.business.model;

/**
 * Kinds of attraction, each admitting different ticket kinds.
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

    /** Returns the type with exactly the given name, or {@code null} if there is none. */
    public static AttractionType fromName(String text) {
        for (AttractionType type : values()) {
            if (type.name().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
