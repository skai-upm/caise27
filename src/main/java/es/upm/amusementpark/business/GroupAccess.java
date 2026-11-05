package es.upm.amusementpark.business;

import java.util.List;

/**
 * Access capability of group tickets (all members of the purchase enter together).
 */
public interface GroupAccess extends GeneralAccess {

    List<IndividualTicket> getComponents();
}
