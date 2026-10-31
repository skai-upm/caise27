package es.upm.amusementpark.presentation.help;

import java.util.List;

/** The commands supported by the application, in the order help lists them. */
public final class CommandRegistry {

    private final List<CommandInfo> commands;

    public CommandRegistry(List<CommandInfo> commands) {
        this.commands = List.copyOf(commands);
    }

    public List<CommandInfo> getCommands() {
        return commands;
    }

    /** Registry of every command the command processor executes. */
    public static CommandRegistry standard() {
        return new CommandRegistry(List.of(
                new CommandInfo("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                        "Registers a user after validating DNI, email, password and card; DNI and email must be unique."),
                new CommandInfo("USER DELETE;Correo;Contraseña",
                        "Deletes an authenticated user that holds no ticket dated today or later."),
                new CommandInfo("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                        "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of an authenticated user."),
                new CommandInfo("SYSTEM DATE;yyyy-MM-dd",
                        "Sets the simulated current date used by every date rule."),
                new CommandInfo("SYSTEM PRICE;Precio",
                        "Sets the base individual ticket price for new purchases."),
                new CommandInfo("GROUP CREATE;IdGrupo;Nombre", "Creates an empty group."),
                new CommandInfo("GROUP ADD;IdGrupo;Correo", "Adds a registered user to a group that is not locked."),
                new CommandInfo("GROUP REMOVE;IdGrupo;Correo", "Removes a member from a group that is not locked."),
                new CommandInfo("GROUP DELETE;IdGrupo", "Deletes a group that is not locked; its users are kept."),
                new CommandInfo("TICKET CREATE;IdTicket;Correo;yyyy-MM-dd",
                        "Buys an individual ticket for a user at the current base price."),
                new CommandInfo("TICKET DELETE;IdTicket",
                        "Refunds an individual ticket dated after the system date."),
                new CommandInfo("GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd",
                        "Buys one ticket per group member with a discount by group size."),
                new CommandInfo("GROUPTICKET DELETE;IdTicket",
                        "Refunds a group ticket dated after the system date together with its member tickets."),
                new CommandInfo("ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]",
                        "Creates an attraction of type GENERAL (default), INDIVIDUAL, GROUP or SPECIAL."),
                new CommandInfo("ATTRACTION ENTER;IdAtraccion;IdTicket[;SPEED]",
                        "Admits the users of a ticket valid today if there is room for all of them; SPEED uses one "
                                + "SpeedPass per user and SPECIAL attractions use the ticket's Special Pass."),
                new CommandInfo("ATTRACTION EXIT;IdAtraccion;IdTicket",
                        "Lets the users of the ticket present in the attraction leave."),
                new CommandInfo("ATTRACTION STATUS;IdAtraccion",
                        "Shows capacity, occupancy, available places and users present."),
                new CommandInfo("SPEEDPASS CREATE;IdTicket;Cantidad",
                        "Buys 1 or 10 SpeedPasses for an individual ticket, each at 20% of the ticket price."),
                new CommandInfo("SPEEDPASS DELETE;IdPase",
                        "Refunds an unused SpeedPass of a ticket dated after the system date."),
                new CommandInfo("SPECIALPASS CREATE;IdTicket",
                        "Buys the only Special Pass an individual ticket may ever have, at the ticket price."),
                new CommandInfo("SPECIALPASS DELETE;IdPase",
                        "Refunds an unused Special Pass of a ticket dated after the system date."),
                new CommandInfo("INVOICE CREATE;USER;Correo",
                        "Shows the invoice of a user's directly bought tickets and personal passes."),
                new CommandInfo("INVOICE CREATE;GROUP;IdGrupo",
                        "Shows the invoice of a group's group tickets."),
                new CommandInfo("HELP", "Lists the syntax of every command."),
                new CommandInfo("HELP EXTENDED", "Lists the syntax and a description of every command."),
                new CommandInfo("EXIT", "Ends the execution.")));
    }
}
