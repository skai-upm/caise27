package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Attraction restricted to one kind of ticket access. The bound of {@code T} is the capability the attraction
 * relies on; tickets that do not provide it are rejected.
 *
 * @param <T> kind of ticket accepted by the attraction
 */
public abstract class TypedAttraction<T extends AccessTicket> extends Attraction {

    private final Class<T> ticketKind;

    protected TypedAttraction(String id, String name, int capacity, Class<T> ticketKind) {
        super(id, name, capacity);
        this.ticketKind = ticketKind;
    }

    /** Returns the ticket typed as accepted by this attraction, or empty if its kind is not admitted. */
    public Optional<T> accept(AccessTicket ticket) {
        return ticketKind.isInstance(ticket) ? Optional.of(ticketKind.cast(ticket)) : Optional.empty();
    }

    @Override
    public final void enter(AccessTicket ticket, LocalDate today) throws AccessDeniedException {
        T typed = accept(ticket).orElseThrow(() -> new AccessDeniedException(
                getType() + " attraction " + getId() + " does not accept ticket " + ticket.getId()));
        enterWith(typed, today);
    }

    /** Template for typed entry: validity, room, then admission. Subclasses may add requirements. */
    protected void enterWith(T ticket, LocalDate today) throws AccessDeniedException {
        if (!ticket.isValidOn(today)) {
            throw new AccessDeniedException("ticket " + ticket.getId() + " is not valid today (" + today + ")");
        }
        checkRoomFor(ticket);
        admit(ticket.getUsers());
    }
}
