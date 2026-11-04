package es.upm.park.presentation;

import es.upm.park.business.model.UserField;
import java.util.Arrays;
import java.util.List;

/** The commands registered in the application, in the order shown by HELP. */
public final class CommandCatalog {

    private static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                    "Registers a user after validating all data; DNI and email must be unique."),
            new CommandInfo("USER DELETE;Correo;Contraseña",
                    "Deletes a user after checking the password, unless it has tickets for today or later."),
            new CommandInfo("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                    "Changes one field of an authenticated user; Campo is one of " + fieldNames() + "."),
            new CommandInfo("GROUP CREATE;Id;Nombre", "Creates an empty group of users."),
            new CommandInfo("GROUP ADD;Id;Correo",
                    "Adds a registered user to a group (not allowed while the group has active group tickets)."),
            new CommandInfo("GROUP REMOVE;Id;Correo",
                    "Removes a user from a group (not allowed while the group has active group tickets)."),
            new CommandInfo("GROUP DELETE;Id",
                    "Deletes a group without deleting its users (not allowed while it has active group tickets)."),
            new CommandInfo("TICKET CREATE;Id;Correo;yyyy-MM-dd",
                    "Buys an individual ticket for a user and a date not before the system date, at the current price."),
            new CommandInfo("TICKET DELETE;Id", "Deletes an individual ticket dated after the system date."),
            new CommandInfo("GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd",
                    "Buys one ticket per current group member, with a discount by group size."),
            new CommandInfo("GROUPTICKET DELETE;IdTicket",
                    "Deletes a group ticket dated after the system date together with its member tickets."),
            new CommandInfo("ATTRACTION CREATE;Id;Nombre;Capacidad", "Creates a GENERAL attraction with a maximum capacity."),
            new CommandInfo("ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo",
                    "Creates an attraction of type GENERAL (all tickets), INDIVIDUAL or SPECIAL (individual tickets)"
                    + " or GROUP (group tickets)."),
            new CommandInfo("ATTRACTION ENTER;IdAtraccion;IdTicket",
                    "Admits the holders of a ticket valid today if there is room for all of them; SPECIAL"
                    + " attractions consume the ticket's Special Pass."),
            new CommandInfo("ATTRACTION ENTER;IdAtraccion;IdTicket;SPEED",
                    "Enters using one unused SpeedPass per individual ticket (per member for group tickets)."),
            new CommandInfo("ATTRACTION EXIT;IdAtraccion;IdTicket",
                    "Lets the holders of a ticket leave the attraction, freeing their places."),
            new CommandInfo("ATTRACTION STATUS;IdAtraccion",
                    "Shows capacity, occupancy, available places and the users present."),
            new CommandInfo("INVOICE CREATE;USER;Correo", "Shows the invoice of a user's individual ticket purchases."),
            new CommandInfo("INVOICE CREATE;GROUP;IdGrupo", "Shows the invoice of a group's group-ticket purchases."),
            new CommandInfo("SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad",
                    "Buys 1 or 10 SpeedPasses for an individual ticket, each at 20% of the ticket price."),
            new CommandInfo("SPEEDPASS DELETE;IdPase",
                    "Refunds an unused SpeedPass whose ticket is dated after the system date."),
            new CommandInfo("SPECIALPASS CREATE;IdPase;IdTicket",
                    "Buys the single Special Pass of an individual ticket, at the ticket price."),
            new CommandInfo("SPECIALPASS DELETE;IdPase",
                    "Refunds an unused Special Pass whose ticket is dated after the system date."),
            new CommandInfo("SYSTEM DATE;yyyy-MM-dd", "Sets the simulated current date used by every date rule."),
            new CommandInfo("SYSTEM PRICE;Precio", "Sets the individual-ticket price for later purchases."),
            new CommandInfo("HELP", "Lists the syntax of every command."),
            new CommandInfo("HELP EXTENDED", "Lists every command with its syntax and a short explanation."),
            new CommandInfo("EXIT", "Stops processing commands and ends the application."));

    private CommandCatalog() {
    }

    public static List<CommandInfo> commands() {
        return COMMANDS;
    }

    static String fieldNames() {
        return String.join(", ", Arrays.stream(UserField.values()).map(Enum::name).toList());
    }
}
