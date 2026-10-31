package es.upm.amusementpark.business.model;

/** Kinds of attraction, by the tickets they accept. */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    /** Creates an empty attraction of this type. */
    public Attraction<? extends AccessTicket> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }

    /** Returns the type with exactly this name, or {@code null} if there is none. */
    public static AttractionType fromName(String name) {
        for (AttractionType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
