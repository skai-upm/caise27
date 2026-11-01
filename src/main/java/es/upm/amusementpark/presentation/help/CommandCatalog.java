package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Registry of the commands the application accepts, used by every help format.
 */
public final class CommandCatalog {

    public static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                    "Registers a user after validating DNI, email, password and card; DNI and email must be unique."),
            new CommandInfo("USER DELETE;Correo;Contraseña",
                    "Deletes an authenticated user who has no ticket dated today or later."),
            new CommandInfo("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                    "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of an authenticated user."),
            new CommandInfo("SYSTEM DATE;yyyy-MM-dd",
                    "Sets the simulated system date used by every date-dependent rule."),
            new CommandInfo("SYSTEM PRICE;Precio",
                    "Sets the base price of individual tickets bought from now on."),
            new CommandInfo("GROUP CREATE;IdGrupo;Nombre", "Creates an empty group."),
            new CommandInfo("GROUP ADD;IdGrupo;Correo",
                    "Adds a registered user to a group that has no current or future group ticket."),
            new CommandInfo("GROUP REMOVE;IdGrupo;Correo",
                    "Removes a member from a group that has no current or future group ticket."),
            new CommandInfo("GROUP DELETE;IdGrupo",
                    "Deletes a group that has no current or future group ticket; its users are kept."),
            new CommandInfo("TICKET CREATE;IdTicket;Correo;yyyy-MM-dd",
                    "Buys an individual ticket for a user and date at the current base price."),
            new CommandInfo("TICKET DELETE;IdTicket",
                    "Refunds an individual ticket dated after the system date."),
            new CommandInfo("GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd",
                    "Buys one ticket per current group member with a discount by group size."),
            new CommandInfo("GROUPTICKET DELETE;IdTicket",
                    "Refunds a group ticket dated after the system date, with all its member tickets."),
            new CommandInfo("ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad",
                    "Creates an attraction with a maximum capacity."),
            new CommandInfo("ATTRACTION ENTER;IdAtraccion;IdTicket",
                    "Lets the users of a ticket valid today enter, all or none, within capacity."),
            new CommandInfo("ATTRACTION EXIT;IdAtraccion;IdTicket",
                    "Lets the users of a ticket leave the attraction, freeing their places."),
            new CommandInfo("ATTRACTION STATUS;IdAtraccion",
                    "Shows capacity, occupancy, available places and users present."),
            new CommandInfo("INVOICE CREATE;USER;Correo",
                    "Shows the invoice of a user's directly bought tickets and of the passes on their tickets."),
            new CommandInfo("INVOICE CREATE;GROUP;IdGrupo",
                    "Shows the invoice of a group's group tickets."),
            new CommandInfo("SPEEDPASS CREATE;IdTicket;Cantidad",
                    "Buys 1 or 10 SpeedPasses for an individual ticket, each at 20% of the ticket price."),
            new CommandInfo("SPEEDPASS DELETE;IdPase",
                    "Refunds an unused SpeedPass of a ticket dated after the system date."),
            new CommandInfo("SPECIALPASS CREATE;IdTicket",
                    "Buys the only Special Pass an individual ticket can ever have, at the ticket price."),
            new CommandInfo("SPECIALPASS DELETE;IdPase",
                    "Refunds an unused Special Pass of a ticket dated after the system date."),
            new CommandInfo("HELP", "Lists the syntax of every command."),
            new CommandInfo("HELP EXTENDED", "Lists the syntax and a description of every command."),
            new CommandInfo("EXIT", "Ends the application; later commands in a file are not executed."));

    private CommandCatalog() {
    }
}
