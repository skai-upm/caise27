package es.upm.etsisi.amusementpark.business.model;

/**
 * Group access capability: a ticket that admits all members included in a group purchase.
 */
public interface GroupAdmission extends Admission {

    /** The purchasing group, or {@code null} once it has been deleted. */
    Group getGroup();
}
