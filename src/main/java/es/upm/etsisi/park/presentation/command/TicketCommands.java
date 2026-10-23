package es.upm.etsisi.park.presentation.command;

import es.upm.etsisi.park.business.controller.TicketController;
import es.upm.etsisi.park.business.model.ticket.GroupTicket;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.presentation.Args;
import es.upm.etsisi.park.presentation.CommandDefinition;
import es.upm.etsisi.park.presentation.CommandGroup;
import es.upm.etsisi.park.presentation.CommandRegistry;

/** TICKET CREATE/DELETE and GROUPTICKET CREATE/DELETE. */
public class TicketCommands implements CommandGroup {

    static final String CREATE = "TICKET CREATE;<identifier>;<email>;<date>";
    static final String DELETE = "TICKET DELETE;<identifier>";
    static final String GROUP_CREATE = "GROUPTICKET CREATE;<identifier>;<group>;<date>";
    static final String GROUP_DELETE = "GROUPTICKET DELETE;<identifier>";

    private final TicketController tickets;

    public TicketCommands(TicketController tickets) {
        this.tickets = tickets;
    }

    @Override
    public void register(CommandRegistry registry) {
        registry.register(CommandDefinition.of("TICKET CREATE",
                "Buys an individual ticket for a user, valid on a date from today on, at the current base price.",
                (args, out) -> {
                    Args.require(args, CREATE, 3);
                    IndividualTicket ticket = tickets.createTicket(args[0], args[1], args[2]);
                    out.println("TICKET CREATED: " + ticket.getId() + " for " + ticket.getDate() + ", price "
                            + ticket.getFormattedPrice());
                }, CREATE));
        registry.register(CommandDefinition.of("TICKET DELETE",
                "Refunds an individual ticket dated after today, together with all its passes.",
                (args, out) -> {
                    Args.require(args, DELETE, 1);
                    tickets.deleteTicket(args[0]);
                    out.println("TICKET DELETED");
                }, DELETE));
        registry.register(CommandDefinition.of("GROUPTICKET CREATE",
                "Buys a ticket for every current member of a group (ids <identifier>:<dni>) with a size discount.",
                (args, out) -> {
                    Args.require(args, GROUP_CREATE, 3);
                    GroupTicket ticket = tickets.createGroupTicket(args[0], args[1], args[2]);
                    out.println("GROUP TICKET CREATED: " + ticket.getId() + " for " + ticket.getDate() + ", "
                            + ticket.getMembers().size() + " member(s), " + ticket.getDiscountPercent()
                            + "% discount, total " + ticket.getFormattedPrice());
                }, GROUP_CREATE).withNotes("discount: <3 members 0%, 3-5 10%, 6-10 20%, >10 50%"));
        registry.register(CommandDefinition.of("GROUPTICKET DELETE",
                "Refunds a group ticket dated after today, with its member tickets and all their passes.",
                (args, out) -> {
                    Args.require(args, GROUP_DELETE, 1);
                    tickets.deleteGroupTicket(args[0]);
                    out.println("GROUP TICKET DELETED");
                }, GROUP_DELETE));
    }
}
