package es.upm.parque.presentation;

import java.util.List;

/**
 * The registered commands of the application. It is the single source for help output and for the usage shown
 * when a command has a wrong number of parameters.
 */
public final class CommandCatalog {

    public static final CommandInfo USER_CREATE = new CommandInfo(
        "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
        "Registers a user. DNI (8 digits + check letter), email (texto@texto.xxx), password (upper, lower, digit, "
            + "symbol) and card (16 digits) are validated; DNI and email must be unique.");
    public static final CommandInfo USER_DELETE = new CommandInfo(
        "USER DELETE;Correo;Contraseña",
        "Deletes the user if the password matches and it has no ticket dated on or after the system date; "
            + "it also leaves its groups and attractions.");
    public static final CommandInfo USER_UPDATE = new CommandInfo(
        "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
        "Changes one field (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) after checking email and password; "
            + "the new value is validated again. The DNI cannot be changed.");
    public static final CommandInfo GROUP_CREATE = new CommandInfo(
        "GROUP CREATE;Id;Nombre", "Creates an empty group with a unique identifier.");
    public static final CommandInfo GROUP_ADD = new CommandInfo(
        "GROUP ADD;Id;Correo",
        "Adds a registered user to the group once; rejected while the group has current or future group tickets.");
    public static final CommandInfo GROUP_REMOVE = new CommandInfo(
        "GROUP REMOVE;Id;Correo",
        "Removes a member from the group; rejected while the group has current or future group tickets.");
    public static final CommandInfo GROUP_DELETE = new CommandInfo(
        "GROUP DELETE;Id",
        "Deletes the group (members stay registered); rejected while it has current or future group tickets.");
    public static final CommandInfo TICKET_CREATE = new CommandInfo(
        "TICKET CREATE;Id;Correo;yyyy-MM-dd",
        "Sells an individual ticket to a registered user for a date not before the system date, at the current price.");
    public static final CommandInfo TICKET_DELETE = new CommandInfo(
        "TICKET DELETE;Id",
        "Deletes an individual ticket dated after the system date. Tickets of a group ticket are not deleted here.");
    public static final CommandInfo GROUPTICKET_CREATE = new CommandInfo(
        "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
        "Buys a ticket for every current member of the group (Id-1, Id-2, ...) with a discount by size: "
            + "<3 0%, 3-5 10%, 6-10 20%, >10 50%.");
    public static final CommandInfo GROUPTICKET_DELETE = new CommandInfo(
        "GROUPTICKET DELETE;Id",
        "Deletes a group ticket dated after the system date together with its internal tickets.");
    public static final CommandInfo ATTRACTION_CREATE = new CommandInfo(
        "ATTRACTION CREATE;Id;Nombre;Capacidad", "Creates an attraction with a positive maximum capacity.");
    public static final CommandInfo ATTRACTION_DELETE = new CommandInfo(
        "ATTRACTION DELETE;Id", "Deletes an attraction that has nobody inside.");
    public static final CommandInfo ATTRACTION_ENTER = new CommandInfo(
        "ATTRACTION ENTER;Id;IdTicket",
        "Lets in the users of a ticket valid on the system date; all of them enter or none if capacity is short.");
    public static final CommandInfo ATTRACTION_EXIT = new CommandInfo(
        "ATTRACTION EXIT;Id;IdTicket", "Lets out the users of the ticket who are inside, freeing their places.");
    public static final CommandInfo ATTRACTION_STATUS = new CommandInfo(
        "ATTRACTION STATUS;Id", "Shows capacity, users present, available places and who is inside.");
    public static final CommandInfo INVOICE_USER = new CommandInfo(
        "INVOICE CREATE;USER;Correo", "Prints the invoice of a user: individual tickets bought for that user.");
    public static final CommandInfo INVOICE_GROUP = new CommandInfo(
        "INVOICE CREATE;GROUP;IdGrupo", "Prints the invoice of a group: its group tickets with discounts.");
    public static final CommandInfo SYSTEM_DATE = new CommandInfo(
        "SYSTEM DATE;yyyy-MM-dd", "Sets the system date used by every date rule (not persisted).");
    public static final CommandInfo SYSTEM_PRICE = new CommandInfo(
        "SYSTEM PRICE;Importe", "Sets the price of tickets sold from now on (starts at 40.00, not persisted).");
    public static final CommandInfo HELP = new CommandInfo("HELP", "Lists the syntax of every command.");
    public static final CommandInfo HELP_EXTENDED = new CommandInfo(
        "HELP EXTENDED", "Lists every command with a brief description of its behavior.");
    public static final CommandInfo EXIT = new CommandInfo("EXIT", "Ends the application; later commands are ignored.");

    private static final List<CommandInfo> ALL = List.of(USER_CREATE, USER_DELETE, USER_UPDATE, GROUP_CREATE,
        GROUP_ADD, GROUP_REMOVE, GROUP_DELETE, TICKET_CREATE, TICKET_DELETE, GROUPTICKET_CREATE, GROUPTICKET_DELETE,
        ATTRACTION_CREATE, ATTRACTION_DELETE, ATTRACTION_ENTER, ATTRACTION_EXIT, ATTRACTION_STATUS, INVOICE_USER,
        INVOICE_GROUP, SYSTEM_DATE, SYSTEM_PRICE, HELP, HELP_EXTENDED, EXIT);

    private CommandCatalog() {
    }

    /** @return every registered command in display order */
    public static List<CommandInfo> all() {
        return ALL;
    }
}
