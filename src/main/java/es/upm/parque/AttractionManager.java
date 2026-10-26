package es.upm.parque;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules for attractions: creation, and entry and exit with tickets. Entry is only allowed
 * with tickets dated on the system date held by the {@link MainController}.
 */
public class AttractionManager {

    private final AttractionRepository repository;
    private final TicketManager ticketManager;
    private final MainController mainController;
    private List<Attraction> attractions;

    public AttractionManager(AttractionRepository repository, TicketManager ticketManager,
                             MainController mainController) {
        this.repository = repository;
        this.ticketManager = ticketManager;
        this.mainController = mainController;
        this.attractions = new ArrayList<>(repository.loadAll());
    }

    public List<Attraction> getAttractions() {
        return List.copyOf(attractions);
    }

    public Attraction findById(String id) {
        return attractions.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }

    public void create(String id, String name, String capacityText) {
        if (!UserValidator.isNotBlank(id) || !UserValidator.isNotBlank(name)) {
            throw new ParkException("The attraction identifier and name are required");
        }
        if (findById(id) != null) {
            throw new ParkException("An attraction with identifier " + id + " already exists");
        }
        attractions.add(new Attraction(id, name, parseCapacity(capacityText)));
        persist();
    }

    /**
     * Lets the users of a ticket dated today into an attraction. Returns how many entered.
     */
    public int enter(String attractionId, String ticketId) {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = requireTicket(ticketId);
        if (!ticket.getDate().equals(mainController.getCurrentDate())) {
            throw new ParkException("Ticket " + ticketId + " is valid for " + MainController.formatDate(ticket.getDate())
                    + ", not for the system date " + MainController.formatDate(mainController.getCurrentDate()));
        }
        return attraction.admit(ticket);
    }

    /**
     * Lets the users of a ticket out of an attraction. Returns how many left.
     */
    public int exit(String attractionId, String ticketId) {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = requireTicket(ticketId);
        int left = attraction.release(ticket);
        if (left == 0) {
            throw new ParkException("No user of ticket " + ticketId + " is in attraction " + attractionId);
        }
        return left;
    }

    public Attraction requireAttraction(String id) {
        Attraction attraction = findById(id);
        if (attraction == null) {
            throw new ParkException("No attraction with identifier " + id);
        }
        return attraction;
    }

    private Ticket requireTicket(String id) {
        Ticket ticket = ticketManager.findById(id);
        if (ticket == null) {
            throw new ParkException("No ticket with identifier " + id);
        }
        return ticket;
    }

    private static int parseCapacity(String text) {
        try {
            int capacity = Integer.parseInt(text);
            if (capacity > 0) {
                return capacity;
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        throw new ParkException("Invalid capacity (positive integer expected): " + text);
    }

    private void persist() {
        try {
            repository.saveAll(attractions);
        } catch (UncheckedIOException e) {
            attractions = new ArrayList<>(repository.loadAll());
            throw new ParkException("Could not save attractions: " + e.getMessage());
        }
    }
}
