package es.upm.parque.business.model;

/**
 * Access capability of a ticket that admits exactly one user.
 */
public interface IndividualAdmission extends Admission {

    User getHolder();

    /**
     * The ticket's Special Pass, or null when it has none.
     */
    SpecialPass getSpecialPass();
}
