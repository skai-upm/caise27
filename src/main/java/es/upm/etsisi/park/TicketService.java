package es.upm.etsisi.park;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Individual and group ticket operations over the shared ticket identifier space.
 */
public class TicketService {

    private final Park park;

    public TicketService(Park park) {
        this.park = park;
    }

    public IndividualTicket createIndividual(String id, String email, String dateText) throws ValidationException {
        GroupService.requireText(id, "ticket identifier");
        requireFreeId(id);
        User user = park.findUser(email);
        if (user == null) {
            throw new ValidationException("no user with email " + email);
        }
        LocalDate date = notPast(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, park.getBasePrice(), 0);
        park.addTicket(ticket);
        park.commit(() -> park.removeTicket(ticket));
        return ticket;
    }

    public void deleteIndividual(String id) throws ValidationException {
        Ticket ticket = requireTicket(id);
        if (ticket instanceof GroupTicket) {
            throw new ValidationException("ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        if (!park.isTopLevel(ticket)) {
            throw new ValidationException("ticket " + id + " belongs to a group ticket and cannot be deleted on its own");
        }
        requireFuture(ticket);
        park.removeTicket(ticket);
        park.commit(() -> park.addTicket(ticket));
    }

    public GroupTicket createGroup(String id, String groupId, String dateText) throws ValidationException {
        GroupService.requireText(id, "ticket identifier");
        requireFreeId(id);
        Group group = park.findGroup(groupId);
        if (group == null) {
            throw new ValidationException("no group with identifier " + groupId);
        }
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new ValidationException("group " + groupId + " has no users");
        }
        LocalDate date = notPast(dateText);

        int discount = GroupTicket.discountFor(members.size());
        Set<String> candidateIds = new HashSet<>();
        candidateIds.add(id);
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            String composedId = GroupTicket.composedId(id, member);
            if (!candidateIds.add(composedId) || park.isTicketIdInUse(composedId)) {
                throw new ValidationException("ticket identifier " + composedId + " is already in use");
            }
            composition.add(new IndividualTicket(composedId, date, member, park.getBasePrice(), discount));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, composition);
        park.addTicket(ticket);
        park.commit(() -> park.removeTicket(ticket));
        return ticket;
    }

    public void deleteGroup(String id) throws ValidationException {
        Ticket ticket = requireTicket(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new ValidationException("ticket " + id + " is not a group ticket");
        }
        requireFuture(ticket);
        park.removeTicket(ticket);
        park.commit(() -> park.addTicket(ticket));
    }

    private void requireFreeId(String id) throws ValidationException {
        if (park.isTicketIdInUse(id)) {
            throw new ValidationException("ticket identifier " + id + " is already in use");
        }
    }

    private Ticket requireTicket(String id) throws ValidationException {
        Ticket ticket = park.findTicket(id);
        if (ticket == null) {
            throw new ValidationException("no ticket with identifier " + id);
        }
        return ticket;
    }

    private LocalDate notPast(String dateText) throws ValidationException {
        LocalDate date = SystemService.parseDate(dateText);
        if (date.isBefore(park.getCurrentDate())) {
            throw new ValidationException("date " + date + " is before the current system date " + park.getCurrentDate());
        }
        return date;
    }

    private void requireFuture(Ticket ticket) throws ValidationException {
        if (!ticket.getDate().isAfter(park.getCurrentDate())) {
            throw new ValidationException("ticket " + ticket.getId()
                    + " is not dated after the current system date and is kept as a historical record");
        }
    }
}
