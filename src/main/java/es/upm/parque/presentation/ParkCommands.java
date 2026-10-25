package es.upm.parque.presentation;

import java.util.Arrays;
import java.util.List;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.model.UserField;
import es.upm.parque.presentation.command.CommandRegistry;
import es.upm.parque.presentation.command.SimpleCommand;
import es.upm.parque.presentation.help.HelpGenerator;

/**
 * Registers every command of the application, translating text parameters into controller calls.
 */
public final class ParkCommands {

    private ParkCommands() {
    }

    /**
     * Controllers the commands delegate to, configured at startup.
     */
    public record Controllers(ParkController park, UserController users, GroupController groups,
            TicketController tickets, PassController passes, AttractionController attractions,
            InvoiceController invoices) {
    }

    public static void register(CommandRegistry registry, CommandProcessor processor, Controllers c,
            HelpGenerator help, HelpGenerator extendedHelp) {
        registry.register(new SimpleCommand("USER CREATE",
                "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "Registers a user after validating the DNI letter, email, password strength and 16-digit card;"
                        + " DNI and email must be unique.",
                p -> c.users().createUser(p[0], p[1], p[2], p[3], p[4], p[5], p[6])));
        registry.register(new SimpleCommand("USER DELETE", "USER DELETE;Correo;Contraseña",
                "Deletes the user if the password matches and it holds no ticket dated on or after the system"
                        + " date; its past tickets and group memberships are removed.",
                p -> c.users().deleteUser(p[0], p[1])));
        registry.register(new SimpleCommand("USER UPDATE", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD after checking the current credentials;"
                        + " the DNI cannot be changed.",
                p -> userUpdate(c.users(), p)));
        registry.register(new SimpleCommand("SYSTEM DATE", "SYSTEM DATE;yyyy-MM-dd",
                "Sets the simulated current date used by every date rule (initially today).",
                p -> c.park().setSystemDate(p[0])));
        registry.register(new SimpleCommand("SYSTEM PRICE", "SYSTEM PRICE;Precio",
                "Sets the individual ticket price for new purchases (initially 40.00).",
                p -> c.park().setTicketPrice(p[0])));
        registry.register(new SimpleCommand("GROUP CREATE", "GROUP CREATE;Id;Nombre",
                "Creates an empty group with a unique identifier.",
                p -> c.groups().createGroup(p[0], p[1])));
        registry.register(new SimpleCommand("GROUP ADD", "GROUP ADD;Id;Correo",
                "Adds an existing user to the group once; not allowed while the group has a current or future"
                        + " group ticket.",
                p -> c.groups().addToGroup(p[0], p[1])));
        registry.register(new SimpleCommand("GROUP REMOVE", "GROUP REMOVE;Id;Correo",
                "Removes a member from the group; not allowed while the group has a current or future group"
                        + " ticket.",
                p -> c.groups().removeFromGroup(p[0], p[1])));
        registry.register(new SimpleCommand("GROUP DELETE", "GROUP DELETE;Id",
                "Deletes the group (not its users) and its past group tickets; not allowed while it has a current"
                        + " or future group ticket.",
                p -> c.groups().deleteGroup(p[0])));
        registry.register(new SimpleCommand("TICKET CREATE", "TICKET CREATE;Id;Correo;yyyy-MM-dd",
                "Buys an individual ticket for an existing user at the current price for a date not before the"
                        + " system date.",
                p -> c.tickets().createTicket(p[0], p[1], p[2])));
        registry.register(new SimpleCommand("TICKET DELETE", "TICKET DELETE;Id",
                "Deletes an individually bought ticket dated after the system date, with all its passes.",
                p -> c.tickets().deleteTicket(p[0])));
        registry.register(new SimpleCommand("GROUPTICKET CREATE", "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
                "Buys a group ticket: one ticket <Id>-<DNI> per current member, with a 0/10/20/50% discount for"
                        + " <3, 3-5, 6-10, >10 members.",
                p -> c.tickets().createGroupTicket(p[0], p[1], p[2])));
        registry.register(new SimpleCommand("GROUPTICKET DELETE", "GROUPTICKET DELETE;Id",
                "Deletes a group ticket dated after the system date with its member tickets and their passes.",
                p -> c.tickets().deleteGroupTicket(p[0])));
        registry.register(new SimpleCommand("SPEEDPASS CREATE", "SPEEDPASS CREATE;IdEntrada;Cantidad",
                "Buys 1 or 10 SpeedPasses for an individual ticket, each costing 20% of the ticket price.",
                p -> c.passes().createSpeedPasses(p[0], p[1])));
        registry.register(new SimpleCommand("SPEEDPASS DELETE", "SPEEDPASS DELETE;IdPase",
                "Refunds an unused SpeedPass of a ticket dated after the system date.",
                p -> c.passes().deleteSpeedPass(p[0])));
        registry.register(new SimpleCommand("SPECIALPASS CREATE", "SPECIALPASS CREATE;IdEntrada",
                "Buys the only Special Pass an individual ticket can have, at the ticket price.",
                p -> c.passes().createSpecialPass(p[0])));
        registry.register(new SimpleCommand("SPECIALPASS DELETE", "SPECIALPASS DELETE;IdPase",
                "Refunds an unused Special Pass of a ticket dated after the system date.",
                p -> c.passes().deleteSpecialPass(p[0])));
        registry.register(new SimpleCommand("ATTRACTION CREATE",
                List.of("ATTRACTION CREATE;Id;Nombre;Capacidad", "ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo"),
                "Creates an attraction of type GENERAL (default), INDIVIDUAL, GROUP or SPECIAL with a maximum"
                        + " capacity.",
                p -> c.attractions().createAttraction(p[0], p[1], p[2], p.length > 3 ? p[3] : null)));
        registry.register(new SimpleCommand("ATTRACTION DELETE", "ATTRACTION DELETE;Id",
                "Deletes an attraction with nobody inside.",
                p -> c.attractions().deleteAttraction(p[0])));
        registry.register(new SimpleCommand("ATTRACTION ENTER",
                List.of("ATTRACTION ENTER;IdAtraccion;IdEntrada", "ATTRACTION ENTER;IdAtraccion;IdEntrada;SPEED"),
                "Lets in the ticket's users if it is for the system date, admitted by the attraction type and"
                        + " fits; SPEED consumes one SpeedPass per user, SPECIAL attractions consume the Special"
                        + " Pass. All or nobody enters.",
                p -> c.attractions().enterAttraction(p[0], p[1], p.length > 2 ? p[2] : null)));
        registry.register(new SimpleCommand("ATTRACTION EXIT", "ATTRACTION EXIT;IdAtraccion;IdEntrada",
                "Lets out the ticket's users and frees their places.",
                p -> c.attractions().exitAttraction(p[0], p[1])));
        registry.register(new SimpleCommand("ATTRACTION STATUS", "ATTRACTION STATUS;IdAtraccion",
                "Shows identifier, name, type, capacity, occupancy, available places and users present.",
                p -> c.attractions().attractionStatus(p[0])));
        registry.register(new SimpleCommand("INVOICE CREATE",
                List.of("INVOICE CREATE;USER;Correo", "INVOICE CREATE;GROUP;IdGrupo"),
                "Shows an invoice: for a user, the tickets bought directly and all personal passes; for a group,"
                        + " its group tickets. Not stored.",
                p -> c.invoices().createInvoice(p[0], p[1])));
        registry.register(new SimpleCommand("HELP", "HELP", "Shows the syntax of every command.",
                p -> help.generate()));
        registry.register(new SimpleCommand("HELP EXTENDED", "HELP EXTENDED",
                "Shows the syntax and a description of every command.", p -> extendedHelp.generate()));
        registry.register(new SimpleCommand("EXIT", "EXIT", "Stops processing commands and ends the application.",
                p -> {
                    processor.finish();
                    return "OK EXIT";
                }));
    }

    private static String userUpdate(UserController users, String[] p) {
        if ("DNI".equals(p[2])) {
            return "ERROR The DNI cannot be modified";
        }
        UserField field = UserField.fromName(p[2]);
        if (field == null) {
            return "ERROR Unknown field: " + p[2] + ". Editable fields: " + Arrays.toString(UserField.values());
        }
        return users.updateUser(p[0], p[1], field, p[3]);
    }
}
