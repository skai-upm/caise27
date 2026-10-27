package es.upm.amusementpark.command;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.park.ParkController;

/**
 * GROUPTICKET CREATE / DELETE commands.
 */
class GroupTicketCommands {

    private final ParkController park;

    GroupTicketCommands(ParkController park) {
        this.park = park;
    }

    String create(String[] parts) {
        if (parts.length != 4 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use GROUPTICKET CREATE;<identifier>;<group>;<date>";
        }
        String id = parts[1];
        Group group = park.getGroups().findById(parts[2]);
        LocalDate date = Dates.parse(parts[3]);
        if (!park.getTickets().isIdAvailable(id)) {
            return "ERROR: Ticket identifier already exists";
        }
        if (group == null) {
            return "ERROR: Group not found";
        }
        if (date == null) {
            return "ERROR: Invalid date. Use yyyy-MM-dd";
        }
        if (date.isBefore(park.getCurrentDate())) {
            return "ERROR: Tickets cannot be created for past dates";
        }
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            return "ERROR: Group has no members";
        }
        Set<String> derived = new HashSet<>();
        for (User member : members) {
            String memberTicketId = GroupTicket.derivedId(id, member);
            if (!derived.add(memberTicketId) || !park.getTickets().isIdAvailable(memberTicketId)) {
                return "ERROR: Ticket identifier already exists: " + memberTicketId;
            }
        }
        GroupTicket ticket = new GroupTicket(id, date, park.getCurrentPrice(), group, members);
        park.getTickets().add(ticket);
        return "GROUPTICKET CREATED - Total price: " + ticket.getPrice().toPlainString() + " EUR";
    }

    String delete(String[] parts) {
        if (parts.length != 2 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use GROUPTICKET DELETE;<identifier>";
        }
        Ticket ticket = park.getTickets().findById(parts[1]);
        if (!(ticket instanceof GroupTicket)) {
            return "ERROR: Group ticket not found";
        }
        if (!ticket.isDeletable(park.getCurrentDate())) {
            return "ERROR: Only group tickets dated after the current date can be deleted";
        }
        park.getTickets().remove(ticket);
        return "GROUPTICKET DELETED";
    }
}
