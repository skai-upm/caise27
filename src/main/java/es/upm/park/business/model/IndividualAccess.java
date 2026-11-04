package es.upm.park.business.model;

/** Access capability of an individual ticket: it admits exactly its owner. */
public interface IndividualAccess extends Admissible {

    /** The owning user, or {@code null} if the owner was deleted. */
    User getOwner();
}
