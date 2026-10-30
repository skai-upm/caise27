package es.upm.amusementpark.business.model;

/**
 * Attraction types, each restricting the kind of ticket admitted.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /** Creates an attraction of this type. */
    public Attraction create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /** Parses a type name ignoring case, returning {@code null} if unknown. */
    public static AttractionType parse(String text) {
        for (AttractionType type : values()) {
            if (type.name().equalsIgnoreCase(text.trim())) {
                return type;
            }
        }
        return null;
    }
}
