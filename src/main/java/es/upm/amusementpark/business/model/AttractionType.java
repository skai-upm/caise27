package es.upm.amusementpark.business.model;

import es.upm.amusementpark.business.exception.InvalidDataException;

/**
 * Attraction types and the ticket access each one accepts.
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

    public static AttractionType fromName(String name) {
        for (AttractionType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        throw new InvalidDataException("unknown attraction type " + name + " (allowed: GENERAL, INDIVIDUAL, GROUP, "
                + "SPECIAL)");
    }
}
