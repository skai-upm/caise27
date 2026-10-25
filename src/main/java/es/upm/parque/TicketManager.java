package es.upm.parque;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps all tickets (standalone individual tickets and group tickets with their internal tickets).
 */
public class TicketManager {

    private final TicketRepository repository;
    private final List<Ticket> tickets;

    public TicketManager(TicketRepository repository, UserManager userManager, GroupManager groupManager)
            throws IOException {
        this.repository = repository;
        this.tickets = new ArrayList<>(repository.load(userManager::findByDni, groupManager::find));
    }

    /**
     * Top-level tickets: standalone individual tickets and group tickets.
     */
    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    /**
     * Finds any ticket by id, including individual tickets inside group tickets.
     */
    public Ticket find(String id) {
        for (Ticket t : tickets) {
            if (t.getId().equals(id)) {
                return t;
            }
            if (t instanceof GroupTicket gt) {
                for (IndividualTicket it : gt.getTickets()) {
                    if (it.getId().equals(id)) {
                        return it;
                    }
                }
            }
        }
        return null;
    }

    /**
     * All individual tickets, including those inside group tickets.
     */
    public List<IndividualTicket> allIndividualTickets() {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it) {
                result.add(it);
            } else if (t instanceof GroupTicket gt) {
                result.addAll(gt.getTickets());
            }
        }
        return result;
    }

    public void add(Ticket ticket) throws IOException {
        tickets.add(ticket);
        repository.save(tickets);
    }

    public void remove(Ticket ticket) throws IOException {
        tickets.remove(ticket);
        repository.save(tickets);
    }

    /**
     * True if the user has any individual ticket (own or from a group ticket) dated on or after the date.
     */
    public boolean hasActiveTicket(User user, LocalDate currentDate) {
        for (IndividualTicket t : allIndividualTickets()) {
            if (t.getUser() == user && t.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if the group has any group ticket dated on or after the date.
     */
    public boolean isGroupLocked(Group group, LocalDate currentDate) {
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket gt && gt.getGroup() == group && gt.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }
}
