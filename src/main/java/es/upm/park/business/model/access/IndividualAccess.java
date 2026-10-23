package es.upm.park.business.model.access;

import es.upm.park.business.model.User;

/**
 * Access capability of a ticket for exactly one user. INDIVIDUAL and
 * SPECIAL attractions only handle tickets with this capability.
 */
public interface IndividualAccess extends GeneralAccess {

    User getUser();
}
