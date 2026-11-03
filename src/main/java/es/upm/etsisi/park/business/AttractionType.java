package es.upm.etsisi.park.business;

/**
 * Kinds of attraction, each restricting the tickets it accepts.
 */
public enum AttractionType {
    GENERAL,
    INDIVIDUAL,
    GROUP,
    SPECIAL;

    public static AttractionType parse(String text) throws ValidationException {
        for (AttractionType type : values()) {
            if (type.name().equals(text)) {
                return type;
            }
        }
        throw new ValidationException("invalid attraction type " + text + ": expected GENERAL, INDIVIDUAL, GROUP or SPECIAL");
    }

    public Attraction<? extends GeneralAccess> create(String id, String name, int capacity) {
        return switch (this) {
            case GENERAL -> new GeneralAttraction(id, name, capacity);
            case INDIVIDUAL -> new IndividualAttraction(id, name, capacity);
            case GROUP -> new GroupAttraction(id, name, capacity);
            case SPECIAL -> new SpecialAttraction(id, name, capacity);
        };
    }
}
