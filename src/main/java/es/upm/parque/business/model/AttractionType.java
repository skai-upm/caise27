package es.upm.parque.business.model;

/**
 * Kinds of attraction, by the tickets they accept. Each kind creates its own attraction.
 */
public enum AttractionType {

    /** Individual and group tickets. */
    GENERAL {
        @Override
        public Attraction<Admission> create(String id, String name, int capacity) {
            return new Attraction<>(id, name, capacity, this, Admission.class);
        }
    },
    /** Individual tickets only (standalone or internal to a group ticket). */
    INDIVIDUAL {
        @Override
        public Attraction<IndividualAccess> create(String id, String name, int capacity) {
            return new Attraction<>(id, name, capacity, this, IndividualAccess.class);
        }
    },
    /** Group tickets only. */
    GROUP {
        @Override
        public Attraction<GroupAccess> create(String id, String name, int capacity) {
            return new Attraction<>(id, name, capacity, this, GroupAccess.class);
        }
    },
    /** Individual tickets with an available Special Pass. */
    SPECIAL {
        @Override
        public Attraction<IndividualAccess> create(String id, String name, int capacity) {
            return new SpecialAttraction(id, name, capacity);
        }
    };

    public abstract Attraction<? extends Admission> create(String id, String name, int capacity);
}
