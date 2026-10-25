package com.amusementpark.presentation.help;

import java.util.List;

/**
 * Registry of the commands understood by the application, in the order they are presented.
 */
public final class CommandCatalog {

    private static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                    "Registers a user after validating DNI, email, password and card; DNI and email must be unique."),
            new CommandInfo("USER DELETE;Correo;Contraseña",
                    "Deletes the user if the password is correct and they hold no ticket dated on or after the system date."),
            new CommandInfo("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                    "Changes one field (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) after checking the current password."),
            new CommandInfo("SYSTEM DATE;yyyy-MM-dd",
                    "Sets the simulated system date used by every date-dependent rule."),
            new CommandInfo("SYSTEM PRICE;Precio",
                    "Sets the price of the individual tickets bought from now on (40.00 at start-up)."),
            new CommandInfo("GROUP CREATE;Id;Nombre",
                    "Creates an empty group with a unique identifier."),
            new CommandInfo("GROUP ADD;Id;Correo",
                    "Adds a registered user to the group unless already a member or the group is locked by a ticket."),
            new CommandInfo("GROUP REMOVE;Id;Correo",
                    "Removes a member from the group unless the group is locked by a current or future group ticket."),
            new CommandInfo("GROUP DELETE;Id",
                    "Deletes the group unless it has a group ticket dated on or after the system date."),
            new CommandInfo("TICKET CREATE;Id;Correo;yyyy-MM-dd",
                    "Buys an individual ticket for the user and date at the current price."),
            new CommandInfo("TICKET DELETE;Id",
                    "Deletes an individual ticket dated after the system date, together with its passes."),
            new CommandInfo("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
                    "Buys one ticket per current group member with a discount by group size (0/10/20/50%)."),
            new CommandInfo("GROUPTICKET DELETE;Id",
                    "Deletes a group ticket dated after the system date, its member tickets and their passes."),
            new CommandInfo("SPEEDPASS CREATE;IdTicket;Cantidad",
                    "Buys 1 or 10 SpeedPasses for an individual ticket, each costing 20% of the ticket price."),
            new CommandInfo("SPEEDPASS DELETE;IdPase",
                    "Refunds an unused SpeedPass whose ticket is dated after the system date."),
            new CommandInfo("SPECIALPASS CREATE;IdTicket",
                    "Buys the single Special Pass of an individual ticket at the ticket price."),
            new CommandInfo("SPECIALPASS DELETE;IdPase",
                    "Refunds an unused Special Pass whose ticket is dated after the system date."),
            new CommandInfo("ATTRACTION CREATE;Id;Nombre;Capacidad",
                    "Creates a GENERAL attraction with the given maximum capacity."),
            new CommandInfo("ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo",
                    "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL."),
            new CommandInfo("ATTRACTION DELETE;Id",
                    "Deletes an attraction with nobody inside."),
            new CommandInfo("ATTRACTION ENTER;Id;IdTicket",
                    "Admits the ticket's users if the ticket is for today, its kind is accepted and there is room for all."),
            new CommandInfo("ATTRACTION ENTER;Id;IdTicket;SPEED",
                    "As ENTER, consuming one unused SpeedPass per admitted user (not at SPECIAL attractions)."),
            new CommandInfo("ATTRACTION EXIT;Id;IdTicket",
                    "Lets the ticket's users leave the attraction, freeing their places."),
            new CommandInfo("ATTRACTION STATUS;Id",
                    "Shows identifier, name, capacity, occupancy, free places and the users inside."),
            new CommandInfo("INVOICE CREATE;USER;Correo",
                    "Shows the user's directly bought tickets and the passes of all their tickets, with a total."),
            new CommandInfo("INVOICE CREATE;GROUP;IdGrupo",
                    "Shows the group's group tickets with their amounts and a total."),
            new CommandInfo("HELP",
                    "Lists the available commands."),
            new CommandInfo("HELP EXTENDED",
                    "Lists the available commands with a description of each."),
            new CommandInfo("EXIT",
                    "Ends the application; in file mode, later lines are not executed."));

    private CommandCatalog() {
    }

    public static List<CommandInfo> commands() {
        return COMMANDS;
    }
}
