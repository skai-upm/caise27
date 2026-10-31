package es.upm.amusementpark.business.model;

/** Access for exactly one user, who may also hold a Special Pass. */
public interface IndividualAccess extends AccessTicket {

    User getUser();

    /** The ticket's Special Pass, or {@code null} if it has none. */
    SpecialPass getSpecialPass();
}
