package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.persistence.GroupRepository;
import es.upm.etsisi.park.persistence.TicketRepository;
import es.upm.etsisi.park.persistence.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tickets stored in a text file through a {@link TicketFileStore}. Every question about
 * which tickets belong to a user or group is answered by traversing the stored tickets.
 */
public class FileTicketRepository extends ListRepository<Ticket> implements TicketRepository {

    private final TicketFileStore store;

    public FileTicketRepository(TicketFileStore store, UserRepository users, GroupRepository groups) {
        super(store.load(users::findByDni, groups::findById), Ticket::getId);
        this.store = store;
    }

    @Override
    protected void persist(List<Ticket> current) {
        store.save(current);
    }

    /** Finds a top-level ticket or an individual ticket inside a group ticket. */
    @Override
    public Optional<Ticket> findById(String id) {
        for (Ticket ticket : items) {
            if (ticket.getId().equals(id)) {
                return Optional.of(ticket);
            }
            if (ticket instanceof GroupTicket groupTicket) {
                for (IndividualTicket member : groupTicket.getTickets()) {
                    if (member.getId().equals(id)) {
                        return Optional.of(member);
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isIdInUse(String id) {
        return findById(id).isPresent();
    }

    @Override
    public boolean isTopLevel(Ticket ticket) {
        return items.contains(ticket);
    }

    @Override
    public List<GroupTicket> findByGroup(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : items) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    @Override
    public List<IndividualTicket> findStandaloneByUser(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : items) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                result.add(individual);
            }
        }
        return result;
    }

    @Override
    public List<IndividualTicket> findAllIndividualByUser(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : items) {
            for (IndividualTicket individual : ticket.getAdmittedTickets()) {
                if (individual.getUser() == user) {
                    result.add(individual);
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasActiveGroupTicket(Group group, LocalDate today) {
        for (GroupTicket ticket : findByGroup(group)) {
            if (ticket.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasActiveTicket(User user, LocalDate today) {
        for (Ticket ticket : items) {
            if (ticket.isActiveOn(today) && ticket.getUsers().contains(user)) {
                return true;
            }
        }
        return false;
    }
}
