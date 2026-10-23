package es.upm.park.business.model.attraction;

/**
 * Attraction kinds; each knows how to create its attraction.
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
     * @return the type with that name ignoring case and surrounding spaces, or {@code null}
     */
    public static AttractionType parse(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim().toUpperCase();
        for (AttractionType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        return null;
    }
}
