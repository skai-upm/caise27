package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.GroupTicket;
import es.upm.etsisi.amusementpark.model.IndividualTicket;
import es.upm.etsisi.amusementpark.model.Ticket;
import es.upm.etsisi.amusementpark.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static es.upm.etsisi.amusementpark.controller.UserController.requireValid;

/**
 * Creation and deletion of individual and group tickets.
 */
public class TicketController {

    private final ParkController park;

    public TicketController(ParkController park) {
        this.park = park;
    }

    public IndividualTicket createTicket(String id, String email, String dateText) {
        requireNewId(id);
        User user = park.users().findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        LocalDate date = requireNotPast(dateText);
        IndividualTicket ticket = new IndividualTicket(id, date, user, park.basePrice());
        park.tickets().add(ticket);
        return ticket;
    }

    public void deleteTicket(String id) {
        Ticket ticket = find(id);
        requireValid(ticket instanceof IndividualTicket, "Use GROUPTICKET DELETE for group ticket " + id);
        requireValid(park.tickets().isTopLevel(ticket), "Ticket " + id + " belongs to a group ticket");
        requireDeletable(ticket);
        park.tickets().remove(ticket);
    }

    public GroupTicket createGroupTicket(String id, String groupId, String dateText) {
        requireNewId(id);
        Group group = park.groups().findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
        LocalDate date = requireNotPast(dateText);
        List<User> members = group.getMembers();
        requireValid(!members.isEmpty(), "Group " + groupId + " is empty");

        int discount = GroupTicket.discountFor(members.size());
        BigDecimal price = park.basePrice();
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            String memberId = GroupTicket.memberTicketId(id, member);
            requireNewId(memberId);
            composition.add(new IndividualTicket(memberId, date, member, price, discount));
        }
        GroupTicket ticket = new GroupTicket(id, date, group, composition);
        park.tickets().add(ticket);
        return ticket;
    }

    public void deleteGroupTicket(String id) {
        Ticket ticket = find(id);
        requireValid(ticket instanceof GroupTicket, "Ticket " + id + " is not a group ticket");
        requireDeletable(ticket);
        park.tickets().remove(ticket);
    }

    Ticket find(String id) {
        return park.tickets().findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
    }

    private void requireNewId(String id) {
        requireValid(!id.isBlank(), "Ticket identifier cannot be empty");
        requireValid(!park.tickets().existsId(id), "Ticket identifier already used: " + id);
    }

    private LocalDate requireNotPast(String dateText) {
        LocalDate date = ParkController.parseDate(dateText);
        requireValid(!date.isBefore(park.today()), "Ticket date is in the past: " + date);
        return date;
    }

    private void requireDeletable(Ticket ticket) {
        requireValid(ticket.isDeletableOn(park.today()),
                "Only tickets dated after " + park.today() + " can be deleted");
    }
}
