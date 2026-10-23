package es.upm.amusementpark.business.model;

/**
 * Capability of a ticket that admits exactly one user. Passes are bought for
 * tickets with this capability.
 */
public interface IndividualAccess extends GeneralAccess {

    User getUser();
}
