package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * The commands understood by {@link CommandProcessor}, used to build every kind of help.
 */
public final class CommandRegistry {

    private static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                    "Registers a user after validating DNI, email, password and card; DNI and email must be unique."),
            new CommandInfo("USER DELETE;Correo;Contraseña",
                    "Deletes the user if the password matches and no ticket dated today or later blocks it."),
            new CommandInfo("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                    "Changes one field (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) after checking the password."),
            new CommandInfo("SYSTEM DATE;yyyy-MM-dd",
                    "Sets the simulated current date used by every date-dependent rule."),
            new CommandInfo("SYSTEM PRICE;Precio",
                    "Sets the base ticket price for later purchases (40.00 at start-up)."),
            new CommandInfo("GROUP CREATE;IdGrupo;Nombre", "Creates an empty group."),
            new CommandInfo("GROUP ADD;IdGrupo;Correo",
                    "Adds a registered user to a group that is not locked by a current group ticket."),
            new CommandInfo("GROUP REMOVE;IdGrupo;Correo", "Removes a member from a group that is not locked."),
            new CommandInfo("GROUP DELETE;IdGrupo", "Deletes a group that is not locked; its users remain."),
            new CommandInfo("TICKET CREATE;IdTicket;Correo;yyyy-MM-dd",
                    "Buys an individual ticket for today or a later date at the current base price."),
            new CommandInfo("TICKET DELETE;IdTicket", "Refunds an individual ticket dated after the current date."),
            new CommandInfo("GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd",
                    "Buys one ticket per group member with a size discount (3-5: 10%, 6-10: 20%, >10: 50%)."),
            new CommandInfo("GROUPTICKET DELETE;IdTicket",
                    "Refunds a group ticket dated after the current date together with its member tickets."),
            new CommandInfo("ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]",
                    "Creates an attraction; Tipo is GENERAL (default, individual and group tickets), INDIVIDUAL, "
                    + "GROUP or SPECIAL (individual tickets with a Special Pass)."),
            new CommandInfo("ATTRACTION ENTER;IdAtraccion;IdTicket",
                    "Admits every user of a ticket valid today if there is room for all of them."),
            new CommandInfo("ATTRACTION EXIT;IdAtraccion;IdTicket",
                    "Lets the users of the ticket leave the attraction, freeing their places."),
            new CommandInfo("ATTRACTION STATUS;IdAtraccion",
                    "Shows capacity, occupancy, available places and the users inside."),
            new CommandInfo("SPEEDPASS CREATE;IdTicket;Cantidad",
                    "Buys 1 or 10 SpeedPasses for an individual ticket, each at 20% of the ticket price."),
            new CommandInfo("SPEEDPASS DELETE;IdPase",
                    "Refunds an unused SpeedPass whose ticket is dated after the current date."),
            new CommandInfo("SPECIALPASS CREATE;IdTicket",
                    "Buys the ticket's only Special Pass, at the ticket price."),
            new CommandInfo("SPECIALPASS DELETE;IdPase",
                    "Refunds an unused Special Pass whose ticket is dated after the current date."),
            new CommandInfo("INVOICE CREATE;USER;Correo",
                    "Prints the invoice of a user's direct ticket purchases and personal passes."),
            new CommandInfo("INVOICE CREATE;GROUP;IdGrupo",
                    "Prints the invoice of a group's group-ticket purchases."),
            new CommandInfo("HELP", "Lists the syntax of every command."),
            new CommandInfo("HELP EXTENDED", "Lists every command with its syntax and a short description."),
            new CommandInfo("EXIT", "Ends the session; commands after it are not executed."));

    private CommandRegistry() {
    }

    public static List<CommandInfo> commands() {
        return COMMANDS;
    }
}
