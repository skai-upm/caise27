package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Central park management (Singleton). Holds the current system date and base price and centralises access to the
 * injected repositories of users, groups, tickets, attractions and passes.
 */
public final class Park {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static Park instance;

    private LocalDate date;
    private BigDecimal price;
    private UserRepository users;
    private GroupRepository groups;
    private TicketRepository tickets;
    private AttractionRepository attractions;
    private PassRepository passes;

    private Park() {
    }

    public static synchronized Park getInstance() {
        if (instance == null) {
            instance = new Park();
        }
        return instance;
    }

    /**
     * Injects the repositories of this execution and starts it with today's date and a price of 40.00.
     */
    public void configure(UserRepository users, GroupRepository groups, TicketRepository tickets,
                          AttractionRepository attractions, PassRepository passes) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
        this.passes = passes;
        this.date = LocalDate.now();
        this.price = INITIAL_PRICE;
    }

    public LocalDate getDate() {
        return date;
    }

    void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    void setPrice(BigDecimal price) {
        this.price = price;
    }

    public UserRepository getUsers() {
        return users;
    }

    public GroupRepository getGroups() {
        return groups;
    }

    public TicketRepository getTickets() {
        return tickets;
    }

    public AttractionRepository getAttractions() {
        return attractions;
    }

    public PassRepository getPasses() {
        return passes;
    }

    /**
     * A group is locked while it has a group ticket dated on or after the current system date.
     */
    public boolean isLocked(Group group) {
        for (GroupTicket ticket : tickets.groupTicketsOf(group)) {
            if (ticket.isActiveOn(date)) {
                return true;
            }
        }
        return false;
    }
}
