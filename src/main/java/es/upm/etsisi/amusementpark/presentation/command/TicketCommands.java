package es.upm.etsisi.amusementpark.presentation.command;

import es.upm.etsisi.amusementpark.business.controller.TicketController;

/**
 * TICKET and GROUPTICKET CREATE / DELETE.
 */
public class TicketCommands implements CommandModule {

    private final TicketController tickets;

    public TicketCommands(TicketController tickets) {
        this.tickets = tickets;
    }

    @Override
    public void registerIn(CommandRegistry registry) {
        registry.register("TICKET CREATE;<identifier>;<email>;<date>",
                "Buys an individual ticket for a user at the current base price for today or a later date.",
                p -> "TICKET CREATED - PRICE: " + tickets.createTicket(p[1], p[2], p[3]).getPrice() + " EUR");
        registry.register("TICKET DELETE;<identifier>",
                "Refunds a future stand-alone individual ticket together with its passes.",
                p -> {
                    tickets.deleteTicket(p[1]);
                    return "TICKET DELETED";
                });
        registry.register("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                "Buys a group ticket with one discounted individual ticket per current member.",
                p -> "GROUPTICKET CREATED - TOTAL PRICE: "
                        + tickets.createGroupTicket(p[1], p[2], p[3]).getPrice() + " EUR");
        registry.register("GROUPTICKET DELETE;<identifier>",
                "Refunds a future group ticket, its composition and the members' passes on it.",
                p -> {
                    tickets.deleteGroupTicket(p[1]);
                    return "GROUPTICKET DELETED";
                });
    }
}
