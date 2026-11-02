package es.upm.etsisi.park.business;

/**
 * Attraction kinds and the concrete attraction each one creates.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    public Attraction<?> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /** Parses a type name exactly as written in commands and files. */
    public static AttractionType parse(String name) {
        for (AttractionType t : values()) {
            if (t.name().equals(name)) {
                return t;
            }
        }
        throw new RuleViolationException("Unknown attraction type: " + name);
    }
}
