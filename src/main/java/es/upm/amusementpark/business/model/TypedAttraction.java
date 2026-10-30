package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    public final List<Pass> enter(AccessTicket ticket, boolean speed, LocalDate today) throws AccessDeniedException {
        T typed = accept(ticket).orElseThrow(() -> new AccessDeniedException(
                getType() + " attraction " + getId() + " does not accept ticket " + ticket.getId()));
        if (!typed.isValidOn(today)) {
            throw new AccessDeniedException("ticket " + typed.getId() + " is not valid today (" + today + ")");
        }
        checkRoomFor(typed);
        List<Pass> passes = requiredPasses(typed, speed);
        admit(typed.getUsers());
        for (Pass pass : passes) {
            pass.consume();
        }
        return passes;
    }

    /**
     * Passes that the entry consumes, checked before any change. By default no pass is needed, and SPEED entry
     * needs one unused SpeedPass for every individual ticket admitted.
     */
    protected List<Pass> requiredPasses(T ticket, boolean speed) throws AccessDeniedException {
        List<Pass> passes = new ArrayList<>();
        if (!speed) {
            return passes;
        }
        for (IndividualAccess unit : ticket.getAccessUnits()) {
            SpeedPass pass = unit.findUnusedSpeedPass();
            if (pass == null) {
                throw new AccessDeniedException("ticket " + unit.getId() + " has no unused SpeedPass");
            }
            passes.add(pass);
        }
        return passes;
    }
}
