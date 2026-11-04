package es.upm.etsisi.amusementpark.presentation.help;

import java.util.List;

/** The registry of commands supported by the application, in help order. */
public class CommandCatalog {

    private final List<CommandInfo> commands;

    public CommandCatalog(List<CommandInfo> commands) {
        this.commands = List.copyOf(commands);
    }

    public List<CommandInfo> commands() {
        return commands;
    }

    public List<String> operations() {
        return commands.stream().map(CommandInfo::operation).toList();
    }

    /** The commands of the amusement-park application. */
    public static CommandCatalog standard() {
        return new CommandCatalog(List.of(
                new CommandInfo("USER CREATE", "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                        "Registers a user after validating DNI, email, password and card; DNI and email must be unique."),
                new CommandInfo("USER DELETE", "USER DELETE;Correo;Contraseña",
                        "Deletes the user with that email and password, unless they hold a ticket dated today or later."),
                new CommandInfo("USER UPDATE",
                        "USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
                        "Changes one field of the authenticated user; the DNI cannot be changed."),
                new CommandInfo("GROUP CREATE", "GROUP CREATE;Id;Nombre",
                        "Creates an empty group with a unique identifier."),
                new CommandInfo("GROUP ADD", "GROUP ADD;Id;Correo",
                        "Adds an existing user to a group, unless the group has a ticket dated today or later."),
                new CommandInfo("GROUP REMOVE", "GROUP REMOVE;Id;Correo",
                        "Removes a member from a group, unless the group has a ticket dated today or later."),
                new CommandInfo("GROUP DELETE", "GROUP DELETE;Id",
                        "Deletes a group (not its users), unless it has a ticket dated today or later."),
                new CommandInfo("TICKET CREATE", "TICKET CREATE;Id;Correo;yyyy-MM-dd",
                        "Buys an individual ticket for a user on a date from today on, at the current ticket price."),
                new CommandInfo("TICKET DELETE", "TICKET DELETE;Id",
                        "Cancels an individual ticket whose date is later than today."),
                new CommandInfo("GROUPTICKET CREATE", "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
                        "Buys one ticket per current group member, with a discount that grows with the group size."),
                new CommandInfo("GROUPTICKET DELETE", "GROUPTICKET DELETE;Id",
                        "Cancels a group ticket, and its members' tickets, if its date is later than today."),
                new CommandInfo("ATTRACTION CREATE", "ATTRACTION CREATE;Id;Nombre;Capacidad",
                        "Creates a GENERAL attraction with a maximum number of simultaneous visitors."),
                new CommandInfo("ATTRACTION CREATE", "ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo",
                        "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL (by tickets admitted)."),
                new CommandInfo("ATTRACTION ENTER", "ATTRACTION ENTER;IdAtraccion;IdTicket",
                        "Lets the ticket's visitors in if the ticket is for today and all of them fit."),
                new CommandInfo("ATTRACTION EXIT", "ATTRACTION EXIT;IdAtraccion;IdTicket",
                        "Lets the ticket's visitors out of the attraction, freeing their places."),
                new CommandInfo("ATTRACTION STATUS", "ATTRACTION STATUS;IdAtraccion",
                        "Shows capacity, occupancy, free places and the visitors inside."),
                new CommandInfo("INVOICE CREATE", "INVOICE CREATE;USER;Correo",
                        "Shows the invoice of the tickets bought directly for a user."),
                new CommandInfo("INVOICE CREATE", "INVOICE CREATE;GROUP;IdGrupo",
                        "Shows the invoice of the group tickets bought by a group."),
                new CommandInfo("SPEEDPASS CREATE", "SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad",
                        "Buys 1 or 10 SpeedPasses (ids IdCompra-1..n) for a ticket, each at 20% of the ticket price."),
                new CommandInfo("SPEEDPASS DELETE", "SPEEDPASS DELETE;IdPase",
                        "Refunds an unused SpeedPass whose ticket is for a later date than today."),
                new CommandInfo("SPECIALPASS CREATE", "SPECIALPASS CREATE;IdPase;IdTicket",
                        "Buys the only Special Pass of a ticket, at the ticket price."),
                new CommandInfo("SPECIALPASS DELETE", "SPECIALPASS DELETE;IdPase",
                        "Refunds an unused Special Pass whose ticket is for a later date than today."),
                new CommandInfo("SYSTEM DATE", "SYSTEM DATE;yyyy-MM-dd",
                        "Sets the simulated current date used by every date check."),
                new CommandInfo("SYSTEM PRICE", "SYSTEM PRICE;Precio",
                        "Sets the price of tickets bought from now on in this run."),
                new CommandInfo("HELP", "HELP",
                        "Lists the syntax of every command."),
                new CommandInfo("HELP EXTENDED", "HELP EXTENDED",
                        "Lists every command with its syntax and a short explanation."),
                new CommandInfo("EXIT", "EXIT",
                        "Ends the application; later commands are not processed.")));
    }
}
