package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import java.util.Optional;

/**
 * Pass lookups needed to decide attraction entry.
 */
public interface PassBook {

    Optional<SpeedPass> unusedSpeedPass(IndividualTicket ticket);

    Optional<SpecialPass> specialPass(IndividualTicket ticket);
}
