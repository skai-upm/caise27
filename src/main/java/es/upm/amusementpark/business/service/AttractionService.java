package es.upm.amusementpark.business.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import es.upm.amusementpark.business.exception.ConflictException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.AttractionRepository;

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

    public Attraction createAttraction(String id, String name, int capacity) throws ParkException {
        if (id.isEmpty() || name.isEmpty()) {
            throw new ValidationException("Attraction identifier and name are required");
        }
        if (capacity < 1) {
            throw new ValidationException("Capacity must be at least 1");
        }
        if (repository.findById(id).isPresent()) {
            throw new ConflictException("An attraction with this identifier already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        repository.save(attraction);
        return attraction;
    }

    /**
     * Emails of the users currently inside the attraction. Read-only.
     */
    public List<String> listOccupantEmails(String id) throws ParkException {
        return existing(id).getOccupants().stream().map(User::getEmail).toList();
    }

    /**
     * Lets the users of the ticket enter the attraction. Works for any ticket type through its
     * individual tickets; either all of them enter or none does.
     *
     * @return the users who entered
     */
    public List<User> enter(String attractionId, Ticket ticket, LocalDate today) throws ParkException {
        Attraction attraction = existing(attractionId);
        if (!ticket.getDate().equals(today)) {
            throw new OperationNotAllowedException("Ticket is not valid for the current date");
        }
        List<User> entering = ticket.individualTickets().stream().map(IndividualTicket::getUser).toList();
        for (User user : entering) {
            if (attraction.contains(user)) {
                throw new ConflictException("User is already inside the attraction: " + user.getEmail());
            }
        }
        if (attraction.getOccupants().size() + entering.size() > attraction.getCapacity()) {
            throw new OperationNotAllowedException("Not enough capacity in the attraction");
        }
        entering.forEach(attraction::addOccupant);
        return entering;
    }

    /**
     * Removes the user with that email from the attraction, freeing its place.
     */
    public User exit(String attractionId, String email) throws ParkException {
        Attraction attraction = existing(attractionId);
        User user = attraction.getOccupants().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new OperationNotAllowedException("User is not inside the attraction: " + email));
        attraction.removeOccupant(user);
        return user;
    }

    /**
     * Current status, taken from the attraction's own state.
     */
    public AttractionStatus status(String attractionId) throws ParkException {
        Attraction a = existing(attractionId);
        List<String> emails = a.getOccupants().stream().map(User::getEmail).toList();
        return new AttractionStatus(a.getId(), a.getName(), a.getCapacity(), emails.size(),
                a.getCapacity() - emails.size(), emails);
    }

    Attraction existing(String id) throws ParkException {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Attraction not found"));
    }
}
