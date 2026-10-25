package es.upm.parque.business.model;

/**
 * Access for a single user, which can carry a Special Pass.
 */
public interface IndividualAccess extends Access {

    User getUser();

    SpecialPass getSpecialPass();
}
