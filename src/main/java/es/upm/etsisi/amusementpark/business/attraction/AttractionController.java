package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.BusinessException;
import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;
import es.upm.etsisi.amusementpark.business.user.User;

/**
 * Singleton controller for attractions: creation, entry (with pass consumption) and exit.
 * Occupancy is transient; consumed passes are persisted.
 */
public final class AttractionController {

    private Park park;
    private PassBook passes;

    private AttractionController() {
    }

    private static final class Holder {
        private static final AttractionController INSTANCE = new AttractionController();
    }

    public static AttractionController getInstance() {
        return Holder.INSTANCE;
    }

    public void configure(Park parkState, PassBook passBook) {
        park = parkState;
        passes = passBook;
    }

    public Attraction<?> create(String id, String name, int capacity, AttractionType type) throws BusinessException {
        if (id.isBlank() || name.isBlank()) {
            throw new InvalidDataException("attraction identifier and name must not be empty");
        }
        if (capacity <= 0) {
            throw new InvalidDataException("capacity must be a positive integer");
        }
        if (park.findAttraction(id).isPresent()) {
            throw new OperationNotAllowedException("attraction " + id + " already exists");
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        park.attractions().add(attraction);
        park.commit(() -> park.attractions().remove(attraction));
        return attraction;
    }

    public Attraction<?> require(String id) throws NotFoundException {
        return park.requireAttraction(id);
    }

    /**
     * Admits every holder of the ticket together and consumes the required passes, or changes
     * nothing at all.
     */
    public Entry enter(String attractionId, String ticketId, boolean speed) throws BusinessException {
        Attraction<?> attraction = park.requireAttraction(attractionId);
        Ticket ticket = park.requireTicket(ticketId);
        Entry entry = attraction.prepareEntry(ticket, speed, park.getCurrentDate(), passes);
        attraction.admit(entry.entrants());
        entry.passes().forEach(Pass::consume);
        if (!entry.passes().isEmpty()) {
            park.commit(() -> {
                entry.passes().forEach(Pass::restoreUnconsumed);
                entry.entrants().forEach(attraction::release);
            });
        }
        return entry;
    }

    public void exit(String attractionId, String email) throws BusinessException {
        Attraction<?> attraction = park.requireAttraction(attractionId);
        User occupant = attraction.getOccupants().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new OperationNotAllowedException("user " + email + " is not inside "
                        + attractionId));
        attraction.release(occupant);
    }
}
