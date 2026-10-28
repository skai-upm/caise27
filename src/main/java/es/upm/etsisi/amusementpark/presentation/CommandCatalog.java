package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Help entries of every command recognised by the application, in display order.
 */
public final class CommandCatalog {

    public static final List<CommandHelp> ENTRIES = List.of(
            new CommandHelp("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                    "Registers a new user with their personal, contact and payment data."),
            new CommandHelp("USER DELETE;<Email>;<Password>",
                    "Deletes a user after checking the password, if no active ticket or locked group prevents it."),
            new CommandHelp("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                    "Changes one field of a user after checking the password."),
            new CommandHelp("SYSTEM DATE;<yyyy-MM-dd>",
                    "Sets the current system date used by date-dependent operations."),
            new CommandHelp("SYSTEM PRICE;<Price>",
                    "Sets the base price of tickets created from now on in this execution."),
            new CommandHelp("GROUP CREATE;<Identifier>;<Name>",
                    "Creates an empty group of users."),
            new CommandHelp("GROUP ADD;<Identifier>;<Email>",
                    "Adds an existing user to a group that has no active group ticket."),
            new CommandHelp("GROUP REMOVE;<Identifier>;<Email>",
                    "Removes a user from a group that has no active group ticket."),
            new CommandHelp("GROUP DELETE;<Identifier>",
                    "Deletes a group that has no active group ticket, keeping its historical tickets."),
            new CommandHelp("TICKET CREATE;<identifier>;<email>;<date>",
                    "Buys an individual ticket for a user and date at the current price and shows the price."),
            new CommandHelp("TICKET DELETE;<identifier>",
                    "Deletes an individual ticket dated after the current date."),
            new CommandHelp("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                    "Buys a discounted joint ticket for all current members of a group and shows the total."),
            new CommandHelp("GROUPTICKET DELETE;<identifier>",
                    "Deletes a group ticket dated after the current date, together with its individual tickets."),
            new CommandHelp("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                    "Creates an attraction with its maximum capacity."),
            new CommandHelp("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Lets the user or the whole group of a ticket valid today enter an attraction."),
            new CommandHelp("ATTRACTION EXIT;<attraction>;<email>",
                    "Makes a user leave an attraction, releasing one place."),
            new CommandHelp("ATTRACTION LIST;<attraction>",
                    "Shows the e-mails of the users currently inside an attraction."),
            new CommandHelp("ATTRACTION STATUS;<attractionIdentifier>",
                    "Shows the name, capacity, occupancy, free places and occupants of an attraction."),
            new CommandHelp("INVOICE CREATE;USER;<email>",
                    "Shows the invoice of a user's personal purchases."),
            new CommandHelp("INVOICE CREATE;GROUP;<groupIdentifier>",
                    "Shows the invoice of a group's group ticket purchases."),
            new CommandHelp("HELP",
                    "Lists the syntax of all commands."),
            new CommandHelp("HELP EXTENDED",
                    "Lists the syntax of all commands with a brief explanation of each."),
            new CommandHelp("EXIT",
                    "Ends the application; no later command is executed."));

    private CommandCatalog() {
    }
}
