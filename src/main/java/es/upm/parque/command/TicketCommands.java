package es.upm.parque.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.model.Group;
import es.upm.parque.model.GroupTicket;
import es.upm.parque.model.IndividualTicket;
import es.upm.parque.model.Ticket;
import es.upm.parque.model.User;
import es.upm.parque.park.ParkController;
import es.upm.parque.park.SystemValues;

/**
 * Executes the TICKET and GROUPTICKET commands against the central park management.
 */
class TicketCommands {

    static final String TICKET_CREATE = "TICKET CREATE";
    static final String TICKET_DELETE = "TICKET DELETE";
    static final String GROUPTICKET_CREATE = "GROUPTICKET CREATE";
    static final String GROUPTICKET_DELETE = "GROUPTICKET DELETE";

    private final ParkController park;

    TicketCommands(ParkController park) {
        this.park = park;
    }

    String create(String[] e) {
        if (e.length != 4 || e[1].isEmpty()) {
            return error("TICKET CREATE expects: TICKET CREATE;<id>;<email>;<yyyy-MM-dd>");
        }
        String id = e[1];
        if (park.getTickets().exists(id)) {
            return error("a ticket with identifier " + id + " already exists");
        }
        User user = park.getUsers().findByEmail(e[2]);
        if (user == null) {
            return error("no user with email " + e[2]);
        }
        String problem = dateProblem(e[3]);
        if (problem != null) {
            return error(problem);
        }
        LocalDate date = SystemValues.parseDate(e[3]);
        IndividualTicket ticket = new IndividualTicket(id, user, date, park.getBasePrice());
        park.getTickets().add(ticket);
        return "OK: ticket " + id + " created for " + user.getEmail() + " on " + date
                + ", price " + ticket.getPrice().toPlainString() + " EUR";
    }

    String delete(String[] e) {
        if (e.length != 2) {
            return error("TICKET DELETE expects: TICKET DELETE;<id>");
        }
        Ticket ticket = park.getTickets().findById(e[1]);
        if (!(ticket instanceof IndividualTicket)) {
            return error("no individual ticket with identifier " + e[1]);
        }
        if (park.isPartOfGroupTicket(ticket)) {
            return error("ticket " + e[1] + " belongs to a group ticket and cannot be deleted on its own");
        }
        if (!ticket.isDeletableOn(park.getCurrentDate())) {
            return error("ticket " + e[1] + " is for today or an earlier date and is kept as a historical record");
        }
        park.getTickets().remove(e[1]);
        return "OK: ticket " + e[1] + " deleted";
    }

    String createGroupTicket(String[] e) {
        if (e.length != 4 || e[1].isEmpty()) {
            return error("GROUPTICKET CREATE expects: GROUPTICKET CREATE;<id>;<group>;<yyyy-MM-dd>");
        }
        String id = e[1];
        Group group = park.getGroups().findById(e[2]);
        if (group == null) {
            return error("no group with identifier " + e[2]);
        }
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            return error("group " + e[2] + " is empty; a group ticket needs at least one member");
        }
        String problem = dateProblem(e[3]);
        if (problem != null) {
            return error(problem);
        }
        LocalDate date = SystemValues.parseDate(e[3]);
        List<String> ids = new ArrayList<>();
        ids.add(id);
        members.forEach(m -> ids.add(GroupTicket.componentId(id, m.getDni())));
        for (String candidate : ids) {
            if (park.getTickets().exists(candidate)) {
                return error("ticket identifier " + candidate + " is already in use; nothing was created");
            }
        }
        BigDecimal unitPrice = GroupTicket.discountedPrice(park.getBasePrice(), members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            composition.add(new IndividualTicket(GroupTicket.componentId(id, member.getDni()), member, date, unitPrice));
        }
        GroupTicket groupTicket = new GroupTicket(id, group, date, composition);
        composition.forEach(park.getTickets()::add);
        park.getTickets().add(groupTicket);
        return "OK: group ticket " + id + " created for group " + group.getId() + " on " + date + " ("
                + members.size() + " tickets, discount " + GroupTicket.discountPercentFor(members.size())
                + "%), total " + groupTicket.getPrice().toPlainString() + " EUR";
    }

    String deleteGroupTicket(String[] e) {
        if (e.length != 2) {
            return error("GROUPTICKET DELETE expects: GROUPTICKET DELETE;<id>");
        }
        if (!(park.getTickets().findById(e[1]) instanceof GroupTicket groupTicket)) {
            return error("no group ticket with identifier " + e[1]);
        }
        if (!groupTicket.isDeletableOn(park.getCurrentDate())) {
            return error("group ticket " + e[1] + " is for today or an earlier date and is kept as a historical record");
        }
        groupTicket.getComposition().forEach(t -> park.getTickets().remove(t.getId()));
        park.getTickets().remove(e[1]);
        return "OK: group ticket " + e[1] + " deleted with its " + groupTicket.getComposition().size() + " tickets";
    }

    /** Why the text is not an acceptable ticket date, or {@code null} if it is. */
    private String dateProblem(String text) {
        LocalDate date = SystemValues.parseDate(text);
        if (date == null) {
            return "invalid date '" + text + "' (expected yyyy-MM-dd)";
        }
        return date.isBefore(park.getCurrentDate())
                ? "tickets cannot be created for a date before " + park.getCurrentDate() : null;
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
