package es.upm.park.business.model;

import es.upm.park.business.exception.BusinessRuleException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A park attraction with a maximum capacity and the users currently inside it
 * (referenced by email). The type parameter is the ticket access capability the
 * attraction accepts; only tickets implementing it can be admitted.
 *
 * @param <T> accepted ticket capability
 */
public abstract class Attraction<T extends AccessTicket> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accepted;
    private final Set<String> occupantEmails = new LinkedHashSet<>();

    protected Attraction(String id, String name, int capacity, Class<T> accepted) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.accepted = accepted;
    }

    public abstract AttractionType getType();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    /** Read-only view of the current occupants, in order of entry. */
    public Set<String> getOccupantEmails() {
        return Collections.unmodifiableSet(occupantEmails);
    }

    public boolean isInside(String email) {
        return occupantEmails.contains(email);
    }

    /** Whether this attraction's type can handle the ticket at all. */
    public boolean canHandle(AccessTicket ticket) {
        return accepted.isInstance(ticket);
    }

    /** Whether entries may use SpeedPasses at this attraction. */
    public boolean acceptsSpeedPasses() {
        return true;
    }

    /**
     * Admits the users of a ticket if this attraction accepts its kind and every
     * condition holds. All conditions are checked before anything changes: either
     * everybody enters and the required passes are consumed, or nothing changes.
     *
     * @param speed whether the entry uses one SpeedPass per individual ticket
     */
    public final EntryResult admit(AccessTicket ticket, LocalDate today, EntryRules rules, boolean speed) {
        if (!canHandle(ticket)) {
            throw new BusinessRuleException("Attraction " + id + " (" + getType() + ") does not accept ticket "
                    + ticket.getId());
        }
        if (speed && !acceptsSpeedPasses()) {
            throw new BusinessRuleException("Attraction " + id + " (" + getType() + ") does not accept SpeedPasses");
        }
        T typed = accepted.cast(ticket);
        if (!typed.isValidOn(today)) {
            throw new BusinessRuleException("Ticket " + typed.getId() + " is not valid on " + today);
        }
        checkEntry(typed, rules);
        List<String> entrants = typed.admittedUsers();
        for (String email : entrants) {
            if (isInside(email)) {
                throw new BusinessRuleException("User " + email + " is already inside attraction " + id);
            }
        }
        int free = capacity - occupantEmails.size();
        if (entrants.size() > free) {
            throw new BusinessRuleException("Attraction " + id + " has " + free + " free places for "
                    + entrants.size() + " users");
        }
        List<String> toConsume = new ArrayList<>();
        if (speed) {
            for (IndividualAccess individual : typed.individualTickets()) {
                toConsume.add(rules.availableSpeedPass(individual).orElseThrow(() -> new BusinessRuleException(
                        "Ticket " + individual.getId() + " has no unconsumed SpeedPass")));
            }
        }
        toConsume.addAll(passesToConsume(typed, rules));
        entrants.forEach(occupantEmails::add);
        toConsume.forEach(rules::consume);
        return new EntryResult(entrants, toConsume);
    }

    /** Admits a ticket without SpeedPasses and returns who entered. */
    public final List<String> admit(AccessTicket ticket, LocalDate today, EntryRules rules) {
        return admit(ticket, today, rules, false).entrants();
    }

    /** Additional conditions of a specific attraction type; none by default. Must not change state. */
    protected void checkEntry(T ticket, EntryRules rules) {
        // no extra conditions
    }

    /** Passes a specific attraction type consumes on a successful entry; none by default. */
    protected List<String> passesToConsume(T ticket, EntryRules rules) {
        return List.of();
    }

    /** @return true if the user was added, false if already inside */
    public boolean enter(String email) {
        return occupantEmails.add(email);
    }

    /** @return true if the user was inside and left */
    public boolean leave(String email) {
        return occupantEmails.remove(email);
    }
}
