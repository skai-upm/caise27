package es.upm.amusementpark.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.AttractionRepository;

/**
 * Attraction management.
 */
public class AttractionService {

    private final AttractionRepository repository;

    public AttractionService(AttractionRepository repository) {
        this.repository = repository;
    }

    public Optional<Attraction> findById(String id) {
        return repository.findById(id);
    }

    public List<Attraction> findAll() {
        return repository.findAll();
    }

    public Attraction createAttraction(String id, String name, int capacity) throws OperationException {
        if (id.isEmpty() || name.isEmpty()) {
            throw new OperationException("Attraction identifier and name are required");
        }
        if (capacity < 1) {
            throw new OperationException("Capacity must be at least 1");
        }
        if (repository.findById(id).isPresent()) {
            throw new OperationException("An attraction with this identifier already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        repository.save(attraction);
        return attraction;
    }

    /**
     * Emails of the users currently inside the attraction. Read-only.
     */
    public List<String> listOccupantEmails(String id) throws OperationException {
        return existing(id).getOccupants().stream().map(User::getEmail).toList();
    }

    /**
     * Lets the users of the ticket enter the attraction. Works for any ticket type through its
     * individual tickets; either all of them enter or none does.
     *
     * @return the users who entered
     */
    public List<User> enter(String attractionId, Ticket ticket, LocalDate today) throws OperationException {
        Attraction attraction = existing(attractionId);
        if (!ticket.getDate().equals(today)) {
            throw new OperationException("Ticket is not valid for the current date");
        }
        List<User> entering = ticket.individualTickets().stream().map(IndividualTicket::getUser).toList();
        for (User user : entering) {
            if (attraction.contains(user)) {
                throw new OperationException("User is already inside the attraction: " + user.getEmail());
            }
        }
        if (attraction.getOccupants().size() + entering.size() > attraction.getCapacity()) {
            throw new OperationException("Not enough capacity in the attraction");
        }
        entering.forEach(attraction::addOccupant);
        return entering;
    }

    Attraction existing(String id) throws OperationException {
        return repository.findById(id).orElseThrow(() -> new OperationException("Attraction not found"));
    }
}
