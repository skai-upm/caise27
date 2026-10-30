package es.upm.etsisi.amusementpark.business.model;

/**
 * Access capability of a ticket that admits all members of a group purchase together.
 */
public interface GroupAccess extends TicketAccess {

    /**
     * Purchasing group, or null once that group has been deleted.
     */
    Group getGroup();
}
