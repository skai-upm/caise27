package es.upm.parque.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Business operations on individual and group tickets in the shared identifier space.
 */
public class TicketManager {

    private static final String HISTORICAL = " is for today or an earlier date and is kept as a historical record";

    private ParkController park;

    private TicketManager() {
    }

    private static final class Holder {
        private static final TicketManager INSTANCE = new TicketManager();
    }

    /** Returns the only instance of this controller. */
    public static TicketManager getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the central park management configured during application initialization. */
    public void inject(ParkController park) {
        this.park = park;
    }

    private ParkController park() {
        return ParkConfiguration.requireInjected(park);
    }

    /** Creates an individual ticket at the current base price. */
    public IndividualTicket createIndividual(String id, String email, String dateText) throws ParkException {
        if (park().getTickets().exists(id)) {
            throw new RuleViolationException("a ticket with identifier " + id + " already exists");
        }
        User user = park().getUsers().findByEmail(email);
        if (user == null) {
            throw new NotFoundException("no user with email " + email);
        }
        LocalDate date = ticketDate(dateText);
        IndividualTicket ticket = new IndividualTicket(id, user, date, park().getBasePrice());
        park().getTickets().add(ticket);
        park().persist();
        return ticket;
    }

    /** Deletes a stand-alone individual ticket dated strictly after the current date, together with its passes. */
    public IndividualTicket deleteIndividual(String id) throws ParkException {
        if (!(park().getTickets().findById(id) instanceof IndividualTicket ticket)) {
            throw new NotFoundException("no individual ticket with identifier " + id);
        }
        if (park().isPartOfGroupTicket(ticket)) {
            throw new RuleViolationException("ticket " + id + " belongs to a group ticket and cannot be deleted on its own");
        }
        if (!ticket.isDeletableOn(park().getCurrentDate())) {
            throw new RuleViolationException("ticket " + id + HISTORICAL);
        }
        removePassesOf(ticket);
        park().getTickets().remove(id);
        park().persist();
        return ticket;
    }

    /** Creates a group ticket for all current members, checking every identifier before creating anything. */
    public GroupTicket createGroupTicket(String id, String groupId, String dateText) throws ParkException {
        Group group = park().getGroups().findById(groupId);
        if (group == null) {
            throw new NotFoundException("no group with identifier " + groupId);
        }
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new RuleViolationException("group " + groupId + " is empty; a group ticket needs at least one member");
        }
        LocalDate date = ticketDate(dateText);
        List<String> ids = new ArrayList<>();
        ids.add(id);
        members.forEach(m -> ids.add(GroupTicket.componentId(id, m.getDni())));
        for (String candidate : ids) {
            if (park().getTickets().exists(candidate)) {
                throw new RuleViolationException("ticket identifier " + candidate + " is already in use; nothing was created");
            }
        }
        BigDecimal unitPrice = GroupTicket.discountedPrice(park().getBasePrice(), members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            composition.add(new IndividualTicket(GroupTicket.componentId(id, member.getDni()), member, date, unitPrice));
        }
        GroupTicket groupTicket = new GroupTicket(id, group, date, composition);
        composition.forEach(park().getTickets()::add);
        park().getTickets().add(groupTicket);
        park().persist();
        return groupTicket;
    }

    /**
     * Deletes a group ticket dated strictly after the current date: first the passes of its composition, then the
     * composition, then the group ticket. Every check is made before anything is removed.
     */
    public GroupTicket deleteGroupTicket(String id) throws ParkException {
        if (!(park().getTickets().findById(id) instanceof GroupTicket groupTicket)) {
            throw new NotFoundException("no group ticket with identifier " + id);
        }
        if (!groupTicket.isDeletableOn(park().getCurrentDate())) {
            throw new RuleViolationException("group ticket " + id + HISTORICAL);
        }
        groupTicket.getComposition().forEach(this::removePassesOf);
        groupTicket.getComposition().forEach(t -> park().getTickets().remove(t.getId()));
        park().getTickets().remove(id);
        park().persist();
        return groupTicket;
    }

    /**
     * Number of passes associated with the ticket: those of an individual ticket, or those of every ticket in the
     * composition of a group ticket; 0 for an unknown identifier.
     */
    public int passCount(String id) {
        Ticket ticket = park().getTickets().findById(id);
        if (ticket instanceof GroupTicket groupTicket) {
            return groupTicket.getComposition().stream().mapToInt(t -> park().getPasses().passesOf(t).size()).sum();
        }
        return ticket instanceof IndividualTicket individual ? park().getPasses().passesOf(individual).size() : 0;
    }

    private void removePassesOf(IndividualTicket ticket) {
        park().getPasses().passesOf(ticket).forEach(p -> park().getPasses().remove(p.getId()));
    }

    public Ticket find(String id) throws NotFoundException {
        Ticket ticket = park().getTickets().findById(id);
        if (ticket == null) {
            throw new NotFoundException("no ticket with identifier " + id);
        }
        return ticket;
    }

    private LocalDate ticketDate(String text) throws ParkException {
        LocalDate date = SystemValues.parseDate(text);
        if (date == null) {
            throw new InvalidDataException("invalid date '" + text + "' (expected yyyy-MM-dd)");
        }
        if (date.isBefore(park().getCurrentDate())) {
            throw new RuleViolationException("tickets cannot be created for a date before " + park().getCurrentDate());
        }
        return date;
    }
}
