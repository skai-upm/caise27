package es.upm.parque.presentation;

import es.upm.parque.business.controller.TicketController;

/**
 * TICKET and GROUPTICKET commands.
 */
public class TicketCommands implements CommandSet {

    private final TicketController tickets;

    public TicketCommands(TicketController tickets) {
        this.tickets = tickets;
    }

    @Override
    public void registerInto(CommandRegistry registry) {
        registry.register("TICKET CREATE", "IdEntrada;Correo;Fecha",
                "Buys an individual ticket for a user and date at the current price.",
                p -> "OK TICKET CREATED -> " + tickets.createIndividual(p[0], p[1], p[2]).getPrice().toPlainString());
        registry.register("TICKET DELETE", "IdEntrada",
                "Deletes a future individual ticket and its passes.",
                p -> {
                    tickets.deleteIndividual(p[0]);
                    return "OK TICKET DELETED";
                });
        registry.register("GROUPTICKET CREATE", "IdEntrada;IdGrupo;Fecha",
                "Buys a group ticket: one individual ticket per member, with a discount by group size.",
                p -> "OK GROUP TICKET CREATED -> "
                        + tickets.createGroupTicket(p[0], p[1], p[2]).getPrice().toPlainString());
        registry.register("GROUPTICKET DELETE", "IdEntrada",
                "Deletes a future group ticket with its individual tickets and their passes.",
                p -> {
                    tickets.deleteGroupTicket(p[0]);
                    return "OK GROUP TICKET DELETED";
                });
    }
}
