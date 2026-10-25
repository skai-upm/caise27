package es.upm.parque.presentation.command;

import es.upm.parque.business.controller.TicketController;
import es.upm.parque.presentation.CommandRegistry;

/**
 * TICKET and GROUPTICKET commands.
 */
public final class TicketCommands {

    private TicketCommands() {
    }

    public static void register(CommandRegistry registry, TicketController tickets) {
        registry.register("TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd",
                "Buys an individual ticket for a registered user at the current price, for today or a later date.",
                p -> "OK TICKET CREATED -> " + tickets.createTicket(p[1], p[2], p[3]).getPrice().toPlainString());
        registry.register("TICKET DELETE;IdEntrada",
                "Cancels an individual ticket dated after today, with its passes.",
                p -> {
                    tickets.deleteTicket(p[1]);
                    return "OK TICKET DELETED";
                });
        registry.register("GROUPTICKET CREATE;IdEntrada;IdGrupo;yyyy-MM-dd",
                "Buys one ticket per current group member; discount 0/10/20/50% for <3/3-5/6-10/>10 members.",
                p -> "OK GROUP TICKET CREATED -> "
                        + tickets.createGroupTicket(p[1], p[2], p[3]).getPrice().toPlainString());
        registry.register("GROUPTICKET DELETE;IdEntrada",
                "Cancels a group ticket dated after today with its member tickets and their passes.",
                p -> {
                    tickets.deleteGroupTicket(p[1]);
                    return "OK GROUP TICKET DELETED";
                });
    }
}
