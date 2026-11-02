package es.upm.etsisi.amusementpark.business.pass;

import java.util.Optional;

import es.upm.etsisi.amusementpark.business.ticket.IndividualAccess;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;

/**
 * Pass lookups attractions need during entry, without depending on the controller.
 */
public interface PassBook {
    Optional<SpeedPass> unusedSpeedPass(IndividualTicket ticket);

    Optional<SpecialPass> specialPassOf(IndividualAccess ticket);
}
