package es.upm.amusementpark.business;

/**
 * Attraction types and the attraction each one creates.
 */
public enum AttractionType {
    GENERAL, INDIVIDUAL, GROUP, SPECIAL;

    /** Creates an attraction of this type. */
    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /** @return the type with that exact name, or {@code null} */
    public static AttractionType parse(String text) {
        for (AttractionType type : values()) {
            if (type.name().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
