package es.upm.amusementpark.business.model;

/**
 * Attraction types, which determine the tickets accepted for entry.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /** Returns the type with exactly this name, or null. */
    public static AttractionType fromText(String text) {
        for (AttractionType type : values()) {
            if (type.name().equals(text)) {
                return type;
            }
        }
        return null;
    }

    /** Creates an empty attraction of this type. */
    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }
}
