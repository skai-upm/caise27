package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

/** Registry of the commands supported by the application; the single source used by every help mode. */
public final class CommandCatalog {

    /** Supported commands, in the order they are presented. */
    public static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                    "Registers a user after validating the DNI letter, email, password and 16-digit card; DNI and email must be unique."),
            new CommandInfo("USER DELETE;Correo;Contraseña",
                    "Deletes an authenticated user unless it has tickets dated on or after the system date."),
            new CommandInfo("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                    "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of an authenticated user; the DNI cannot change."),
            new CommandInfo("GROUP CREATE;Id;Nombre", "Creates an empty group with a unique identifier."),
            new CommandInfo("GROUP ADD;Id;Correo",
                    "Adds a registered user to a group; rejected for duplicates or while the group has valid group tickets."),
            new CommandInfo("GROUP REMOVE;Id;Correo",
                    "Removes a member from a group; rejected while the group has valid group tickets."),
            new CommandInfo("GROUP DELETE;Id",
                    "Deletes a group without deleting its users; rejected while it has valid group tickets."),
            new CommandInfo("TICKET CREATE;Id;Correo;yyyy-MM-dd",
                    "Buys an individual ticket for a registered user and a date on or after the system date at the current price."),
            new CommandInfo("TICKET DELETE;Id", "Deletes an individual ticket dated after the system date."),
            new CommandInfo("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
                    "Buys one ticket per current group member with a discount of 0%, 10%, 20% or 50% depending on the group size."),
            new CommandInfo("GROUPTICKET DELETE;Id",
                    "Deletes a group ticket dated after the system date together with its members' tickets."),
            new CommandInfo("SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad",
                    "Buys 1 or 10 SpeedPasses <IdCompra>-n for an individual ticket, each at 20% of the ticket price."),
            new CommandInfo("SPEEDPASS DELETE;IdPase",
                    "Refunds an unused SpeedPass whose ticket is dated after the system date."),
            new CommandInfo("SPECIALPASS CREATE;IdPase;IdTicket",
                    "Buys the only Special Pass of an individual ticket, at the ticket price."),
            new CommandInfo("SPECIALPASS DELETE;IdPase",
                    "Refunds an unused Special Pass whose ticket is dated after the system date."),
            new CommandInfo("ATTRACTION CREATE;Id;Nombre;Capacidad", "Creates an attraction with a maximum capacity."),
            new CommandInfo("ATTRACTION ENTER;IdAtraccion;IdTicket",
                    "Admits the users of an individual or group ticket valid today, all or none, within the capacity."),
            new CommandInfo("ATTRACTION EXIT;IdAtraccion;IdTicket",
                    "Lets the users of a ticket (or the user with that email) leave the attraction, freeing places."),
            new CommandInfo("ATTRACTION STATUS;IdAtraccion",
                    "Shows identifier, name, capacity, users present, available places and the present users."),
            new CommandInfo("INVOICE CREATE;USER;<correo>",
                    "Shows the invoice of a user: tickets bought directly for the user, personal passes and the total."),
            new CommandInfo("INVOICE CREATE;GROUP;<identificadorGrupo>",
                    "Shows the invoice of a group: its group tickets with their discounted totals."),
            new CommandInfo("SYSTEM DATE;yyyy-MM-dd", "Sets the current date used by every date-dependent rule (not persisted)."),
            new CommandInfo("SYSTEM PRICE;Precio", "Sets the individual ticket price for later purchases (40.00 at startup)."),
            new CommandInfo("HELP", "Lists the syntax of every command."),
            new CommandInfo("HELP EXTENDED", "Lists the syntax of every command with a brief description."),
            new CommandInfo("EXIT", "Stops processing commands and ends the application."));

    private CommandCatalog() {
    }
}
