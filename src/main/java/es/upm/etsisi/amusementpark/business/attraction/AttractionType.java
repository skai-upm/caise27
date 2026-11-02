package es.upm.etsisi.amusementpark.business.attraction;

/**
 * Attraction types; each creates the attraction class that enforces its access rules.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    public Attraction<?> create(String identifier, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(identifier, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(identifier, name, capacity);
            case GROUP -> new GroupAttraction(identifier, name, capacity);
            case SPECIAL -> new SpecialAttraction(identifier, name, capacity);
        };
    }

    /**
     * Returns the type with exactly this name, or null if there is none.
     */
    public static AttractionType fromName(String name) {
        for (AttractionType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
