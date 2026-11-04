package es.upm.park;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An individual ticket for a date of use, owned by a registered user, with the price captured
 * at purchase time. It may be bought on its own or be part of a {@link GroupTicket}.
 */
public class IndividualTicket extends Ticket {

    private final User user;

    public IndividualTicket(String id, LocalDate date, BigDecimal price, User user) {
        super(id, date, price);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
