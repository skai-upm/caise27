package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.TicketOperationException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket purchase and deletion on the park's tickets.
 */
public class TicketService {

    private final ParkController park = ParkController.getInstance();

    public IndividualTicket createTicket(String identifier, String email, LocalDate date)
            throws TicketOperationException {
        if (identifier.isEmpty()) {
            throw new TicketOperationException("The ticket identifier cannot be empty.");
        }
        if (park.getTickets().exists(identifier)) {
            throw new TicketOperationException("A ticket with identifier " + identifier + " already exists.");
        }
        User user = park.getUsers().findByEmail(email)
                .orElseThrow(() -> new TicketOperationException("No user with email " + email + " exists."));
        checkNotPast(date);
        IndividualTicket ticket = new IndividualTicket(identifier, date, user, park.getCurrentPrice());
        park.getTickets().save(ticket);
        return ticket;
    }

    public void deleteTicket(String identifier) throws TicketOperationException {
        Ticket ticket = park.getTickets().findById(identifier)
                .orElseThrow(() -> new TicketOperationException("No ticket with identifier " + identifier + " exists."));
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new TicketOperationException("Ticket " + identifier + " is not an individual ticket.");
        }
        if (individual.isPartOfGroupTicket()) {
            throw new TicketOperationException("Ticket " + identifier
                    + " belongs to a group ticket and can only be deleted with it.");
        }
        checkDeletable(ticket);
        park.getTickets().delete(identifier);
    }

    /**
     * Buys a group ticket for all current members of the group. Every
     * identifier is checked before anything is created.
     */
    public GroupTicket createGroupTicket(String identifier, String groupId, LocalDate date)
            throws TicketOperationException {
        if (identifier.isEmpty()) {
            throw new TicketOperationException("The ticket identifier cannot be empty.");
        }
        Group group = park.getGroups().findById(groupId)
                .orElseThrow(() -> new TicketOperationException("No group with identifier " + groupId + " exists."));
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new TicketOperationException("Group " + groupId + " has no members.");
        }
        checkNotPast(date);
        if (park.getTickets().exists(identifier)) {
            throw new TicketOperationException("A ticket with identifier " + identifier + " already exists.");
        }
        for (User member : members) {
            String derived = identifier + ":" + member.getDni();
            if (park.getTickets().exists(derived)) {
                throw new TicketOperationException("A ticket with identifier " + derived + " already exists.");
            }
        }
        BigDecimal price = GroupTicket.discountedPrice(park.getCurrentPrice(), members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            composition.add(new IndividualTicket(identifier + ":" + member.getDni(), date, member, price));
        }
        GroupTicket groupTicket = new GroupTicket(identifier, date, group, composition);
        for (IndividualTicket ticket : composition) {
            park.getTickets().save(ticket);
        }
        park.getTickets().save(groupTicket);
        return groupTicket;
    }

    public void deleteGroupTicket(String identifier) throws TicketOperationException {
        Ticket ticket = park.getTickets().findById(identifier)
                .orElseThrow(() -> new TicketOperationException("No ticket with identifier " + identifier + " exists."));
        if (!(ticket instanceof GroupTicket groupTicket)) {
            throw new TicketOperationException("Ticket " + identifier + " is not a group ticket.");
        }
        checkDeletable(groupTicket);
        for (IndividualTicket composed : groupTicket.getComposition()) {
            park.getTickets().delete(composed.getIdentifier());
        }
        park.getTickets().delete(identifier);
    }

    private void checkNotPast(LocalDate date) throws TicketOperationException {
        if (date.isBefore(park.getCurrentDate())) {
            throw new TicketOperationException("Tickets cannot be created for past dates (current date "
                    + park.getCurrentDate() + ").");
        }
    }

    private void checkDeletable(Ticket ticket) throws TicketOperationException {
        if (!ticket.isDeletableOn(park.getCurrentDate())) {
            throw new TicketOperationException("Ticket " + ticket.getIdentifier()
                    + " is for the current date or earlier and is kept as a historical record.");
        }
    }
}
