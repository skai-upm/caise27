package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.RuleViolationException;


import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.EntryResult;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.PassLookup;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.persistence.repository.AttractionRepository;
import es.upm.amusementpark.persistence.repository.PassRepository;
import java.util.Optional;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Attraction management operations.
 */
public class AttractionService {

    private AttractionRepository attractions;
    private TicketRepository tickets;
    private PassRepository passes = new PassRepository();

    private AttractionService() {
    }

    private static final class Holder {
        private static final AttractionService INSTANCE = new AttractionService();
    }

    /** The single instance of this controller. */
    public static AttractionService getInstance() {
        return Holder.INSTANCE;
    }

    /** Injects the pass repository used for SpeedPass and Special Pass entries. */
    public AttractionService injectPasses(PassRepository passes) {
        this.passes = passes;
        return this;
    }

    private PassLookup lookup() {
        return new PassLookup() {
            @Override
            public Optional<Pass> availableSpeedPass(IndividualTicket ticket) {
                return passes.passesOf(ticket).stream().filter(p -> p instanceof SpeedPass && !p.isConsumed()).findFirst();
            }

            @Override
            public Optional<Pass> specialPass(IndividualTicket ticket) {
                return passes.passesOf(ticket).stream().filter(SpecialPass.class::isInstance).findFirst();
            }
        };
    }

    /** Injects the repositories this controller works with (done during application initialization). */
    public AttractionService inject(AttractionRepository attractions, TicketRepository tickets) {
        this.attractions = attractions;
        this.tickets = tickets;
        return this;
    }



    /**
     * Admits the users of a ticket valid today into an attraction, all or none.
     *
     * @return number of users admitted
     */
    public int enter(String attractionId, String ticketId) {
        return enter(attractionId, ticketId, false).users().size();
    }

    /** Entry with optional SpeedPass use; Special Pass use is automatic at special attractions. */
    public EntryResult enter(String attractionId, String ticketId, boolean speed) {
        Attraction<?> attraction = findAttraction(attractionId);
        Ticket ticket = tickets.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("ticket '" + ticketId + "' not found"));
        LocalDate today = Park.getInstance().getCurrentDate();
        if (!ticket.isValidOn(today)) {
            throw new RuleViolationException("ticket '" + ticketId + "' is valid on " + ticket.getDate()
                    + ", not on the current date " + today);
        }
        EntryResult result = attraction.enter(ticket, speed, lookup());
        attractions.update(attraction);
        if (!result.consumedPasses().isEmpty()) {
            passes.update();
        }
        return result;
    }

    public AttractionRepository attractions() {
        return attractions;
    }

    public Attraction<?> createAttraction(String identifier, String name, String capacityText) {
        return createAttraction(identifier, name, capacityText, AttractionType.GENERAL.name());
    }

    public Attraction<?> createAttraction(String identifier, String name, String capacityText, String typeName) {
        AttractionType type = AttractionType.fromName(typeName).orElseThrow(() ->
                new InvalidDataException("invalid attraction type '" + typeName + "', expected GENERAL, INDIVIDUAL, GROUP or SPECIAL"));
        if (identifier.isEmpty() || name.isEmpty()) {
            throw new InvalidDataException("attraction identifier and name must not be empty");
        }
        if (attractions.findById(identifier).isPresent()) {
            throw new RuleViolationException("an attraction with identifier '" + identifier + "' already exists");
        }
        Attraction<?> attraction = type.create(identifier, name, parseCapacity(capacityText));
        attractions.add(attraction);
        return attraction;
    }

    public Attraction<?> findAttraction(String identifier) {
        return attractions.findById(identifier)
                .orElseThrow(() -> new NotFoundException("attraction '" + identifier + "' not found"));
    }

    public void exit(String attractionId, String email) {
        Attraction<?> attraction = findAttraction(attractionId);
        User user = attraction.getOccupants().stream().filter(u -> u.getEmail().equals(email)).findFirst()
                .orElseThrow(() -> new NotFoundException("user '" + email + "' is not inside " + attractionId));
        attraction.remove(user);
        attractions.update(attraction);
    }

    /** Read-only status of an attraction, from its own state. */
    public String status(String identifier) {
        Attraction<?> a = findAttraction(identifier);
        String users = a.getOccupants().isEmpty() ? "none"
                : String.join(", ", a.getOccupants().stream().map(User::getEmail).toList());
        return String.join(System.lineSeparator(),
                "ATTRACTION " + a.getIdentifier() + " - " + a.getName(),
                "Type: " + a.getType(),
                "Capacity: " + a.getCapacity(),
                "Inside: " + a.getOccupants().size(),
                "Available: " + a.availableCapacity(),
                "Users: " + users);
    }

    /** Read-only listing of the users currently inside an attraction. */
    public String list(String identifier) {
        Attraction<?> a = findAttraction(identifier);
        StringBuilder sb = new StringBuilder("ATTRACTION " + a.getIdentifier() + " (" + a.getName() + ") "
                + a.getOccupants().size() + "/" + a.getCapacity() + ":");
        if (a.getOccupants().isEmpty()) {
            return sb.append(" no users").toString();
        }
        for (User u : a.getOccupants()) {
            sb.append(System.lineSeparator()).append(u.getEmail());
        }
        return sb.toString();
    }

    private static int parseCapacity(String text) {
        boolean digits = !text.isEmpty() && text.length() <= 9;
        for (char c : text.toCharArray()) {
            digits &= c >= '0' && c <= '9';
        }
        int value = digits ? Integer.parseInt(text) : 0;
        if (value <= 0) {
            throw new InvalidDataException("invalid capacity '" + text + "', expected a positive integer");
        }
        return value;
    }
}
