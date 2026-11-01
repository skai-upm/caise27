package es.upm.amusementpark.business.model;

/**
 * Access for exactly one user, which may carry a Special Pass.
 */
public interface IndividualAccess extends AttractionAccess {

    User getUser();

    /**
     * The Special Pass attached to this access, or null if there is none.
     */
    SpecialPass getSpecialPass();
}
