package es.upm.parque.business.model;

/**
 * Access capability of a single user's ticket.
 */
public interface IndividualAdmission extends Admission {

    User getUser();

    /**
     * The Special Pass attached to this ticket, or {@code null} if it has none.
     */
    SpecialPass getSpecialPass();
}
