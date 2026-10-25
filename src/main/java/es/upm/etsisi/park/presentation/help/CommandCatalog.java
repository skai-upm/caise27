package es.upm.etsisi.park.presentation.help;

import java.util.List;

/**
 * Every command the application understands, in display order, with syntax and purpose.
 */
public final class CommandCatalog {

    public static final CommandInfo USER_CREATE = new CommandInfo(
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "Registers a new user after validating DNI, email, password and card.");
    public static final CommandInfo USER_DELETE = new CommandInfo(
            "USER DELETE;<Email>;<Password>",
            "Deletes a user without active tickets or locked groups, after checking the password.");
    public static final CommandInfo USER_UPDATE = new CommandInfo(
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of a user after checking the password.");
    public static final CommandInfo SYSTEM_DATE = new CommandInfo(
            "SYSTEM DATE;<yyyy-MM-dd>",
            "Sets the current system date used by date-dependent operations.");
    public static final CommandInfo SYSTEM_PRICE = new CommandInfo(
            "SYSTEM PRICE;<Price>",
            "Sets the base price for tickets created from now on (40.00 at start-up).");
    public static final CommandInfo GROUP_CREATE = new CommandInfo(
            "GROUP CREATE;<Identifier>;<Name>",
            "Creates an empty group of users.");
    public static final CommandInfo GROUP_ADD = new CommandInfo(
            "GROUP ADD;<Identifier>;<Email>",
            "Adds an existing user to a group that has no active group ticket.");
    public static final CommandInfo GROUP_REMOVE = new CommandInfo(
            "GROUP REMOVE;<Identifier>;<Email>",
            "Removes a member from a group that has no active group ticket.");
    public static final CommandInfo GROUP_DELETE = new CommandInfo(
            "GROUP DELETE;<Identifier>",
            "Deletes a group that has no active group ticket; historical tickets are kept.");
    public static final CommandInfo TICKET_CREATE = new CommandInfo(
            "TICKET CREATE;<identifier>;<email>;<date>",
            "Buys an individual ticket for a user and date at the current price.");
    public static final CommandInfo TICKET_DELETE = new CommandInfo(
            "TICKET DELETE;<identifier>",
            "Refunds a future individual ticket together with all its passes.");
    public static final CommandInfo GROUPTICKET_CREATE = new CommandInfo(
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "Buys a discounted group ticket made of one individual ticket per current member.");
    public static final CommandInfo GROUPTICKET_DELETE = new CommandInfo(
            "GROUPTICKET DELETE;<identifier>",
            "Refunds a future group ticket, its individual tickets and their passes.");
    public static final CommandInfo ATTRACTION_CREATE = new CommandInfo(
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "Creates a GENERAL attraction with a maximum capacity.");
    public static final CommandInfo ATTRACTION_CREATE_TYPED = new CommandInfo(
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
            "Creates a GENERAL, INDIVIDUAL, GROUP or SPECIAL attraction.");
    public static final CommandInfo ATTRACTION_ENTER = new CommandInfo(
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "Lets the holders of a ticket valid today in; SPECIAL attractions consume the Special Pass.");
    public static final CommandInfo ATTRACTION_ENTER_SPEED = new CommandInfo(
            "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
            "Enters using one unconsumed SpeedPass per entering individual ticket (all or nothing).");
    public static final CommandInfo ATTRACTION_EXIT = new CommandInfo(
            "ATTRACTION EXIT;<attraction>;<email>",
            "Takes a user out of an attraction, freeing one place.");
    public static final CommandInfo ATTRACTION_LIST = new CommandInfo(
            "ATTRACTION LIST;<attraction>",
            "Lists the emails of the users currently inside an attraction.");
    public static final CommandInfo ATTRACTION_STATUS = new CommandInfo(
            "ATTRACTION STATUS;<attractionIdentifier>",
            "Shows type, capacity, occupancy, free places and occupants of an attraction.");
    public static final CommandInfo SPEEDPASS_CREATE = new CommandInfo(
            "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
            "Buys 1 or 10 SpeedPasses (20% of the ticket price each) for an individual ticket.");
    public static final CommandInfo SPEEDPASS_DELETE = new CommandInfo(
            "SPEEDPASS DELETE;<speedPassIdentifier>",
            "Refunds an unconsumed SpeedPass of a future ticket.");
    public static final CommandInfo SPECIALPASS_CREATE = new CommandInfo(
            "SPECIALPASS CREATE;<identifier>;<individualTicket>",
            "Buys the Special Pass (same price as the ticket) needed by SPECIAL attractions.");
    public static final CommandInfo SPECIALPASS_DELETE = new CommandInfo(
            "SPECIALPASS DELETE;<specialPassIdentifier>",
            "Refunds an unconsumed Special Pass of a future ticket.");
    public static final CommandInfo INVOICE_USER = new CommandInfo(
            "INVOICE CREATE;USER;<email>",
            "Shows a user's invoice: direct tickets, SpeedPasses and Special Passes.");
    public static final CommandInfo INVOICE_GROUP = new CommandInfo(
            "INVOICE CREATE;GROUP;<groupIdentifier>",
            "Shows a group's invoice: group tickets with their composition.");
    public static final CommandInfo HELP = new CommandInfo("HELP", "Lists the syntax of every command.");
    public static final CommandInfo HELP_EXTENDED = new CommandInfo(
            "HELP EXTENDED", "Lists every command with a description of its purpose.");
    public static final CommandInfo EXIT = new CommandInfo(
            "EXIT", "Ends the application; in file mode later lines are not processed.");

    private static final List<CommandInfo> ALL = List.of(
            USER_CREATE, USER_DELETE, USER_UPDATE,
            SYSTEM_DATE, SYSTEM_PRICE,
            GROUP_CREATE, GROUP_ADD, GROUP_REMOVE, GROUP_DELETE,
            TICKET_CREATE, TICKET_DELETE, GROUPTICKET_CREATE, GROUPTICKET_DELETE,
            ATTRACTION_CREATE, ATTRACTION_CREATE_TYPED, ATTRACTION_ENTER, ATTRACTION_ENTER_SPEED,
            ATTRACTION_EXIT, ATTRACTION_LIST, ATTRACTION_STATUS,
            SPEEDPASS_CREATE, SPEEDPASS_DELETE, SPECIALPASS_CREATE, SPECIALPASS_DELETE,
            INVOICE_USER, INVOICE_GROUP,
            HELP, HELP_EXTENDED, EXIT);

    public List<CommandInfo> commands() {
        return ALL;
    }
}
