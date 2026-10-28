package es.upm.amusementpark.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;

/**
 * Stores every ticket of the system, of any type, in purchase order.
 */
public class TicketRepository {

    private final Map<String, Ticket> tickets = new LinkedHashMap<>();

    public void save(Ticket ticket) {
        tickets.put(ticket.getId(), ticket);
    }

    public void delete(String id) {
        tickets.remove(id);
    }

    public Optional<Ticket> findById(String id) {
        return Optional.ofNullable(tickets.get(id));
    }

    /**
     * Finds an individual ticket, standalone or part of a group ticket composition.
     */
    public Optional<IndividualTicket> findIndividualById(String id) {
        return allIndividualTickets().stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    /**
     * Common identifier check for every ticket type: tickets and their composition share one space.
     */
    public boolean isIdentifierUsed(String id) {
        return tickets.containsKey(id) || findIndividualById(id).isPresent();
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(tickets.values());
    }

    public List<IndividualTicket> allIndividualTickets() {
        List<IndividualTicket> all = new ArrayList<>();
        tickets.values().forEach(t -> all.addAll(t.individualTickets()));
        return all;
    }
}
