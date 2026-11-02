package es.upm.etsisi.amusementpark.business.ticket;

import es.upm.etsisi.amusementpark.business.BusinessException;
import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.PassController;
import es.upm.etsisi.amusementpark.business.user.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Singleton controller for individual and group tickets. Valid deletions cascade to the
 * passes of the deleted tickets.
 */
public final class TicketController {

    private Park park;
    private PassController passes;

    private TicketController() {
    }

    private static final class Holder {
        private static final TicketController INSTANCE = new TicketController();
    }

    public static TicketController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park parkState, PassController passController) {
        park = parkState;
        passes = passController;
    }

    /** Whether the user holds an active individual ticket, standalone or within a group ticket. */
    public boolean hasActiveTicket(User user) {
        return park.allTickets().anyMatch(t -> t instanceof IndividualTicket it
                && it.getUser() == user && it.isActiveOn(park.getCurrentDate()));
    }

    public IndividualTicket create(String id, String email, LocalDate date) throws BusinessException {
        requireText(id);
        User user = park.requireUser(email);
        requireNotPast(date);
        requireUnusedIds(List.of(id));
        IndividualTicket ticket = new IndividualTicket(id, date, user, park.getBasePrice(), 0);
        park.tickets().add(ticket);
        park.commit(() -> park.tickets().remove(ticket));
        return ticket;
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) throws BusinessException {
        requireText(id);
        Group group = park.requireGroup(groupId);
        requireNotPast(date);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new OperationNotAllowedException("group " + groupId + " has no members");
        }
        List<String> ids = new ArrayList<>();
        ids.add(id);
        members.forEach(m -> ids.add(GroupTicket.constituentId(id, m.getDni())));
        requireUnusedIds(ids);
        int discount = GroupTicket.discountFor(members.size());
        List<IndividualTicket> composition = members.stream()
                .map(m -> new IndividualTicket(GroupTicket.constituentId(id, m.getDni()), date, m,
                        park.getBasePrice(), discount))
                .toList();
        GroupTicket ticket = new GroupTicket(id, date, group, composition);
        park.tickets().add(ticket);
        park.commit(() -> park.tickets().remove(ticket));
        return ticket;
    }

    public void delete(String id) throws BusinessException {
        Ticket ticket = park.requireTicket(id);
        if (!park.tickets().contains(ticket)) {
            throw new OperationNotAllowedException("ticket " + id + " belongs to a group ticket");
        }
        if (ticket.isComposite()) {
            throw new OperationNotAllowedException("ticket " + id + " is a group ticket, use GROUPTICKET DELETE");
        }
        deleteWithPasses(ticket);
    }

    public void deleteGroupTicket(String id) throws BusinessException {
        Ticket ticket = park.tickets().stream().filter(t -> t.isComposite() && t.getId().equals(id))
                .findFirst().orElseThrow(() -> new NotFoundException("group ticket " + id + " not found"));
        deleteWithPasses(ticket);
    }

    /** Removes a future ticket and every pass of its individual tickets as one operation. */
    private void deleteWithPasses(Ticket ticket) throws BusinessException {
        if (!ticket.isDeletableOn(park.getCurrentDate())) {
            throw new OperationNotAllowedException("only tickets dated after " + park.getCurrentDate()
                    + " can be deleted");
        }
        List<Pass> passesBefore = List.copyOf(park.passes());
        List<Ticket> ticketsBefore = List.copyOf(park.tickets());
        ticket.getIndividualTickets().forEach(t -> park.passes().removeAll(passes.passesOf(t)));
        park.tickets().remove(ticket);
        park.commit(() -> {
            restore(park.passes(), passesBefore);
            restore(park.tickets(), ticketsBefore);
        });
    }

    private void requireUnusedIds(List<String> ids) throws OperationNotAllowedException {
        Set<String> seen = new HashSet<>();
        for (String id : ids) {
            if (!seen.add(id) || park.findTicket(id).isPresent()) {
                throw new OperationNotAllowedException("ticket identifier " + id + " already in use");
            }
        }
    }

    private void requireNotPast(LocalDate date) throws OperationNotAllowedException {
        if (date.isBefore(park.getCurrentDate())) {
            throw new OperationNotAllowedException("date " + date + " is before the current date "
                    + park.getCurrentDate());
        }
    }

    private static void requireText(String id) throws InvalidDataException {
        if (id.isBlank()) {
            throw new InvalidDataException("ticket identifier must not be empty");
        }
    }

    private static <T> void restore(List<T> target, List<T> snapshot) {
        target.clear();
        target.addAll(snapshot);
    }
}
