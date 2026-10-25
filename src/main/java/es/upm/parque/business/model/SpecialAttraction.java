package es.upm.parque.business.model;

import java.util.List;

/**
 * Admits only individual tickets that hold an unconsumed Special Pass; SPEED is not available.
 */
public class SpecialAttraction extends Attraction<IndividualAccess> {

    public SpecialAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.SPECIAL;
    }

    @Override
    public boolean allowsSpeed() {
        return false;
    }

    @Override
    public boolean requiresSpecialPass() {
        return true;
    }

    @Override
    protected List<User> usersOf(IndividualAccess credential) {
        return List.of(credential.getUser());
    }
}
