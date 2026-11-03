package es.upm.etsisi.amusementpark.business.attraction;

/**
 * Attraction types and the ticket kinds each accepts.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /** Returns the type with the given name, or null when unknown. */
    public static AttractionType fromName(String name) {
        for (AttractionType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public Attraction create(String identifier, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(identifier, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(identifier, name, capacity);
            case GROUP -> new GroupAttraction(identifier, name, capacity);
            case SPECIAL -> new SpecialAttraction(identifier, name, capacity);
        };
    }
}
