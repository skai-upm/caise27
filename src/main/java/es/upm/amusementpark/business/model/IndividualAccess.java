package es.upm.amusementpark.business.model;

import java.util.Optional;

/**
 * Access capability of a ticket that admits exactly one user.
 */
public interface IndividualAccess extends GeneralAccess {

    User getUser();

    Optional<SpecialPass> getSpecialPass();
}
