package es.upm.parque.business.model;

import java.util.List;

/**
 * Admits only individual tickets.
 */
public class IndividualAttraction extends Attraction<IndividualAccess> {

    public IndividualAttraction(String id, String name, int capacity) {
        super(id, name, capacity, IndividualAccess.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.INDIVIDUAL;
    }

    @Override
    protected List<User> usersOf(IndividualAccess credential) {
        return List.of(credential.getUser());
    }
}
