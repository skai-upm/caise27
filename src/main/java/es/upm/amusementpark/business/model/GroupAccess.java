package es.upm.amusementpark.business.model;

/**
 * Access capability of a ticket bought by a group, admitting all its members at once.
 */
public interface GroupAccess extends AccessTicket {

    Group getGroup();
}
