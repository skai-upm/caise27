package es.upm.etsisi.park.business.model.attraction;

import es.upm.etsisi.park.business.exception.ValidationException;

/** Attraction kinds; each creates the attraction class that enforces its access rules. */
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

    /** Type by name, ignoring case and surrounding spaces. */
    public static AttractionType parse(String text) throws ValidationException {
        String normalized = text == null ? "" : text.trim().toUpperCase();
        for (AttractionType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        throw new ValidationException("Invalid attraction type '" + text + "'. Valid types: " + names());
    }

    public static String names() {
        StringBuilder sb = new StringBuilder();
        for (AttractionType type : values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(type.name());
        }
        return sb.toString();
    }
}
