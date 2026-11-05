package es.upm.park.presentation;

import java.util.List;

/**
 * Registry of the commands available in the application, used by every help format.
 */
public final class CommandCatalog {

    private final List<CommandInfo> commands;

    public CommandCatalog(List<CommandInfo> commands) {
        this.commands = List.copyOf(commands);
    }

    public List<CommandInfo> commands() {
        return commands;
    }

    /** The commands of the application, in the order shown by HELP. */
    public static CommandCatalog standard() {
        return new CommandCatalog(List.of(
                new CommandInfo("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                        "Registers a user after validating DNI, email, password and card; DNI and email must be unique."),
                new CommandInfo("USER DELETE;Correo;Contraseña",
                        "Deletes a user after checking the password, unless the user has tickets dated today or later."),
                new CommandInfo("USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
                        "Changes one editable field of a user after checking the current email and password."),
                new CommandInfo("GROUP CREATE;Id;Nombre", "Creates an empty group with a unique identifier."),
                new CommandInfo("GROUP ADD;Id;Correo",
                        "Adds a registered user to a group, unless the group is locked by a current or future group ticket."),
                new CommandInfo("GROUP REMOVE;Id;Correo",
                        "Removes a member from a group, unless the group is locked by a current or future group ticket."),
                new CommandInfo("GROUP DELETE;Id",
                        "Deletes a group (its users are kept), unless it is locked by a current or future group ticket."),
                new CommandInfo("TICKET CREATE;Id;Correo;yyyy-MM-dd",
                        "Buys an individual ticket for a user and a date not before the system date at the current price."),
                new CommandInfo("TICKET DELETE;Id", "Deletes an individual ticket whose date is later than the system date."),
                new CommandInfo("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
                        "Buys one ticket per current group member with a discount by group size and locks the group."),
                new CommandInfo("GROUPTICKET DELETE;Id",
                        "Deletes a group ticket and its individual tickets if its date is later than the system date."),
                new CommandInfo("SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad",
                        "Buys 1 or 10 SpeedPasses (IdCompra-1..n) for an individual ticket, each at 20% of its price."),
                new CommandInfo("SPEEDPASS DELETE;IdPase",
                        "Refunds an unused SpeedPass whose ticket date is later than the system date."),
                new CommandInfo("SPECIALPASS CREATE;Id;IdTicket",
                        "Buys the only Special Pass of an individual ticket at the ticket's price."),
                new CommandInfo("SPECIALPASS DELETE;IdPase",
                        "Refunds an unused Special Pass whose ticket date is later than the system date."),
                new CommandInfo("ATTRACTION CREATE;Id;Nombre;Capacidad", "Creates a GENERAL attraction with a maximum capacity."),
                new CommandInfo("ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo",
                        "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL, which admits only its ticket kinds."),
                new CommandInfo("ATTRACTION ENTER;IdAtraccion;IdTicket",
                        "Admits the users of a ticket valid for the system date if they all fit in the attraction."),
                new CommandInfo("ATTRACTION EXIT;IdAtraccion;IdTicket",
                        "Lets the users of a ticket leave the attraction, freeing their places."),
                new CommandInfo("ATTRACTION STATUS;IdAtraccion",
                        "Shows capacity, occupancy, available places and the users present in an attraction."),
                new CommandInfo("INVOICE CREATE;USER;Correo", "Shows an invoice of the individual tickets bought by a user."),
                new CommandInfo("INVOICE CREATE;GROUP;IdGrupo", "Shows an invoice of the group tickets bought by a group."),
                new CommandInfo("SYSTEM DATE;yyyy-MM-dd", "Sets the simulated current date used by every date rule."),
                new CommandInfo("SYSTEM PRICE;Precio", "Sets the individual ticket price used by later purchases."),
                new CommandInfo("HELP", "Lists the syntax of every command."),
                new CommandInfo("HELP EXTENDED", "Lists the syntax of every command with a short explanation."),
                new CommandInfo("EXIT", "Stops processing commands and ends the application.")));
    }
}
