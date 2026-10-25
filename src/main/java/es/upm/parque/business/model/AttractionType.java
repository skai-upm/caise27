package es.upm.parque.business.model;

/**
 * Kinds of attraction, by the tickets they admit.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /**
     * Returns the type with the given name, or {@code null} if there is none.
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
        switch (this) {
            case INDIVIDUAL:
                return new IndividualAttraction(id, name, capacity);
            case GROUP:
                return new GroupAttraction(id, name, capacity);
            case SPECIAL:
                return new SpecialAttraction(id, name, capacity);
            default:
                return new GeneralAttraction(id, name, capacity);
        }
    }
}
