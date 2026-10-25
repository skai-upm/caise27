package es.upm.parque.business.model;

/**
 * Kinds of attraction, by the tickets they admit.
 */
public enum AttractionType {
    GENERAL {
        @Override
        public Attraction<?> create(String id, String name, int capacity) {
            return new GeneralAttraction(id, name, capacity);
        }
    },
    INDIVIDUAL {
        @Override
        public Attraction<?> create(String id, String name, int capacity) {
            return new IndividualAttraction(id, name, capacity);
        }
    },
    GROUP {
        @Override
        public Attraction<?> create(String id, String name, int capacity) {
            return new GroupAttraction(id, name, capacity);
        }
    },
    SPECIAL {
        @Override
        public Attraction<?> create(String id, String name, int capacity) {
            return new SpecialAttraction(id, name, capacity);
        }
    };

    public abstract Attraction<?> create(String id, String name, int capacity);

    /**
     * Case-insensitive lookup.
     *
     * @throws IllegalArgumentException for an unknown type
     */
    public static AttractionType fromText(String text) {
        for (AttractionType type : values()) {
            if (type.name().equalsIgnoreCase(text.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown attraction type: " + text
                + " (use GENERAL, INDIVIDUAL, GROUP or SPECIAL)");
    }
}
