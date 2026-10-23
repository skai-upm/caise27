package es.upm.park.business.model.pass;

import java.util.List;
import java.util.Optional;

/**
 * Read access to the passes attached to individual tickets, used by
 * attractions to decide which passes an entry consumes.
 */
public interface PassRegistry {

    List<SpeedPass> speedPassesOf(String individualTicketId);

    Optional<SpecialPass> specialPassOf(String individualTicketId);

    default Optional<SpeedPass> firstUnconsumedSpeedPass(String individualTicketId) {
        return speedPassesOf(individualTicketId).stream().filter(p -> !p.isConsumed()).findFirst();
    }
}
