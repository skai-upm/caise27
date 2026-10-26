package es.upm.parque.business.model;

/**
 * Access capability of an individual ticket: it admits one user and may carry a Special Pass.
 */
public interface IndividualAccess extends Admission {

    /** @return the Special Pass of the ticket (in any status), or null if none was bought */
    SpecialPass getSpecialPass();
}
