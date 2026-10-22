package es.upm.etsisi.park.business.model.attraction;

import es.upm.etsisi.park.business.exception.ValidationException;

import java.util.Locale;

/**
 * Attraction types and the concrete attraction each one creates.
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

    /** Case-insensitive lookup. */
    public static AttractionType fromText(String text) {
        try {
            return valueOf(text.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown attraction type: " + text
                    + " (allowed: GENERAL, INDIVIDUAL, GROUP, SPECIAL)");
        }
    }
}
