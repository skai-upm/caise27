package es.upm.amusementpark.business.model.attraction;

/**
 * Kinds of attraction, each restricting the tickets it accepts.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /**
     * Returns the type with exactly the given name, or {@code null} if there is none.
     */
    public static AttractionType fromName(String name) {
        for (AttractionType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }
}
