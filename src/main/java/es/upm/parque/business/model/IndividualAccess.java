package es.upm.parque.business.model;

/**
 * Access capability of a credential that admits exactly one user.
 */
public interface IndividualAccess extends AccessCredential {

    User getUser();
}
