package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Every command recognised by the application, with its syntax and purpose.
 */
public final class CommandCatalog {

    public static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                    "Registers a new user after validating DNI, email, password and card; DNI and email must be unique."),
            new CommandInfo("USER DELETE;<Email>;<Password>",
                    "Deletes a user after checking the password, unless they have active tickets or belong to a locked group."),
            new CommandInfo("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                    "Changes one field (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) of an authenticated user; the DNI cannot change."),
            new CommandInfo("SYSTEM DATE;<yyyy-MM-dd>",
                    "Sets the current system date used by every date-dependent operation."),
            new CommandInfo("SYSTEM PRICE;<Price>",
                    "Sets the base price for tickets created from now on in this execution (initially 40.00)."),
            new CommandInfo("GROUP CREATE;<Identifier>;<Name>",
                    "Creates an empty group of users."),
            new CommandInfo("GROUP ADD;<Identifier>;<Email>",
                    "Adds a registered user to a group that has no active group ticket."),
            new CommandInfo("GROUP REMOVE;<Identifier>;<Email>",
                    "Removes a member from a group that has no active group ticket."),
            new CommandInfo("GROUP DELETE;<Identifier>",
                    "Deletes a group with no active group ticket; its historical group tickets are kept."),
            new CommandInfo("TICKET CREATE;<identifier>;<email>;<date>",
                    "Buys an individual ticket for a user and date (today or later) at the current price."),
            new CommandInfo("TICKET DELETE;<identifier>",
                    "Refunds a future individual ticket together with all its passes."),
            new CommandInfo("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                    "Buys a discounted ticket for every current member of a non-empty group."),
            new CommandInfo("GROUPTICKET DELETE;<identifier>",
                    "Refunds a future group ticket, its member tickets and all their passes."),
            new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                    "Creates a GENERAL attraction with the given capacity."),
            new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                    "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL."),
            new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Enters an attraction with a ticket valid today (SPECIAL attractions consume the Special Pass)."),
            new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                    "Enters using one unconsumed SpeedPass per entering ticket (not allowed at SPECIAL attractions)."),
            new CommandInfo("ATTRACTION EXIT;<attraction>;<email>",
                    "Removes a user from an attraction, freeing one place."),
            new CommandInfo("ATTRACTION LIST;<attraction>",
                    "Lists the emails of the users currently inside an attraction."),
            new CommandInfo("ATTRACTION STATUS;<attractionIdentifier>",
                    "Shows identifier, name, type, capacity, occupancy, free places and current users."),
            new CommandInfo("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                    "Buys 1 or 10 SpeedPasses (20% of the ticket price each) for an individual ticket."),
            new CommandInfo("SPEEDPASS DELETE;<speedPassIdentifier>",
                    "Refunds one unconsumed SpeedPass of a future ticket."),
            new CommandInfo("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                    "Buys the single Special Pass (same price as the ticket) needed for SPECIAL attractions."),
            new CommandInfo("SPECIALPASS DELETE;<specialPassIdentifier>",
                    "Refunds an unconsumed Special Pass of a future ticket."),
            new CommandInfo("INVOICE CREATE;USER;<email>",
                    "Prints the invoice of a user: direct tickets and passes with their total."),
            new CommandInfo("INVOICE CREATE;GROUP;<groupIdentifier>",
                    "Prints the invoice of a group: its group tickets with composition and total."),
            new CommandInfo("HELP",
                    "Shows the syntax of every command."),
            new CommandInfo("HELP EXTENDED",
                    "Shows every command's syntax with a description of its purpose."),
            new CommandInfo("EXIT",
                    "Ends the application; no later command is executed."));

    private CommandCatalog() {
    }
}
