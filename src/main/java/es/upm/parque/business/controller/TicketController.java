package es.upm.parque.business.controller;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.persistence.PassRepository;
import es.upm.parque.persistence.TicketRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps all tickets (standalone individual tickets and group tickets with their internal tickets).
 * Single instance (Singleton); its dependencies are injected with {@code configure} at startup.
 */
public class TicketController {

    private TicketRepository repository;
    private PassRepository passRepository;
    private final List<Ticket> tickets = new ArrayList<>();

    private static final TicketController INSTANCE = new TicketController();

    private TicketController() {
    }

    public static TicketController getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the repositories and the controllers used to resolve users and groups, and loads the stored
     * tickets and their passes.
     */
    public void configure(TicketRepository repository, PassRepository passRepository, UserController userController,
                          GroupController groupController) throws IOException {
        this.repository = repository;
        this.passRepository = passRepository;
        tickets.clear();
        tickets.addAll(repository.load(userController::findByDni, groupController::find));
        passRepository.load(this::find);
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

    /**
     * Removes a top-level ticket. Its passes (and, for a group ticket, its internal tickets and their passes)
     * go with it, since they are owned by the ticket.
     */
    public void remove(Ticket ticket) throws IOException {
        tickets.remove(ticket);
        passRepository.save(allIndividualTickets());
        repository.save(tickets);
    }

    /**
     * Finds a pass by id among all individual tickets.
     */
    public Pass findPass(String id) {
        for (IndividualTicket t : allIndividualTickets()) {
            for (Pass p : t.getPasses()) {
                if (p.getId().equals(id)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Individual ticket that owns the given pass, or null.
     */
    public IndividualTicket ownerOf(Pass pass) {
        for (IndividualTicket t : allIndividualTickets()) {
            if (t.getPasses().contains(pass)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Persists the passes after a purchase, refund or consumption.
     */
    public void savePasses() throws IOException {
        passRepository.save(allIndividualTickets());
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
