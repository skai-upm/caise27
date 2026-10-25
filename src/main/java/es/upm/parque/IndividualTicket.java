package es.upm.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A ticket for one user. It may be bought directly or be part of a {@link GroupTicket}.
 */
public class IndividualTicket extends Ticket {

    private final User user;
    private final String ownerDni;
    private final GroupTicket groupTicket;

    /**
     * @param user        owner, or null for a historical ticket whose user was deleted
     * @param ownerDni    DNI of the owner, kept for history
     * @param groupTicket group ticket this ticket belongs to, or null if bought individually
     */
    public IndividualTicket(String id, LocalDate date, BigDecimal price, User user, String ownerDni,
                            GroupTicket groupTicket) {
        super(id, date, price);
        this.user = user;
        this.ownerDni = ownerDni;
        this.groupTicket = groupTicket;
    }

    public User getUser() {
        return user;
    }

    public String getOwnerDni() {
        return ownerDni;
    }

    public GroupTicket getGroupTicket() {
        return groupTicket;
    }

    public boolean isPartOfGroupTicket() {
        return groupTicket != null;
    }

    @Override
    public List<User> getUsers() {
        return user == null ? List.of() : List.of(user);
    }
}
