package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * All tickets. Top-level tickets are stand-alone individual tickets and group tickets; the
 * individual tickets composing a group ticket share the same identifier space and are
 * found by {@link #findById(String)} too.
 */
public interface TicketRepository extends Repository<Ticket> {

    boolean isIdInUse(String id);

    /** True for stand-alone tickets, false for individual tickets inside a group ticket. */
    boolean isTopLevel(Ticket ticket);

    List<GroupTicket> findByGroup(Group group);

    /** Stand-alone individual tickets bought for the user. */
    List<IndividualTicket> findStandaloneByUser(User user);

    /** Every individual ticket of the user, stand-alone or inside group tickets. */
    List<IndividualTicket> findAllIndividualByUser(User user);

    boolean hasActiveGroupTicket(Group group, LocalDate today);

    /** Whether the user holds an active ticket, stand-alone or inside a group ticket. */
    boolean hasActiveTicket(User user, LocalDate today);
}
