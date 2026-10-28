package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Every command recognized by the application, in the order help shows them.
 */
public final class CommandCatalog {

    private static final List<CommandDescriptor> COMMANDS = List.of(
            new CommandDescriptor("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                    "Registers a new user after validating DNI, email, password and card."),
            new CommandDescriptor("USER DELETE;<Email>;<Password>",
                    "Deletes a user identified by email and password if it has no active tickets."),
            new CommandDescriptor("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                    "Changes one field of a user (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)."),
            new CommandDescriptor("SYSTEM DATE;<yyyy-MM-dd>",
                    "Sets the current system date used by date-dependent operations."),
            new CommandDescriptor("SYSTEM PRICE;<Price>",
                    "Sets the base ticket price for tickets created from now on."),
            new CommandDescriptor("GROUP CREATE;<Identifier>;<Name>",
                    "Creates an empty group of users."),
            new CommandDescriptor("GROUP ADD;<Identifier>;<Email>",
                    "Adds an existing user to a group that is not locked."),
            new CommandDescriptor("GROUP REMOVE;<Identifier>;<Email>",
                    "Removes a user from a group that is not locked."),
            new CommandDescriptor("GROUP DELETE;<Identifier>",
                    "Deletes a group without active group tickets."),
            new CommandDescriptor("TICKET CREATE;<identifier>;<email>;<date>",
                    "Buys an individual ticket for a user and a date, at the current price."),
            new CommandDescriptor("TICKET DELETE;<identifier>",
                    "Deletes an individual ticket dated after the current date."),
            new CommandDescriptor("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                    "Buys a discounted ticket for all current members of a group."),
            new CommandDescriptor("GROUPTICKET DELETE;<identifier>",
                    "Deletes a group ticket dated after the current date."),
            new CommandDescriptor("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                    "Creates an attraction with a maximum capacity."),
            new CommandDescriptor("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Lets the users of a ticket valid today enter an attraction."),
            new CommandDescriptor("ATTRACTION EXIT;<attraction>;<email>",
                    "A user leaves an attraction, freeing a place."),
            new CommandDescriptor("ATTRACTION LIST;<attraction>",
                    "Lists the emails of the users inside an attraction."),
            new CommandDescriptor("ATTRACTION STATUS;<attractionIdentifier>",
                    "Shows capacity, occupation and users of an attraction."),
            new CommandDescriptor("INVOICE CREATE;USER;<email>",
                    "Shows the invoice of a user's personal purchases."),
            new CommandDescriptor("INVOICE CREATE;GROUP;<groupIdentifier>",
                    "Shows the invoice of a group's group tickets."),
            new CommandDescriptor("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                    "Buys 1 or 10 SpeedPasses for an individual ticket (20% of its price each)."),
            new CommandDescriptor("SPEEDPASS DELETE;<speedPassIdentifier>",
                    "Refunds an unconsumed SpeedPass of a ticket dated after the current date."),
            new CommandDescriptor("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                    "Buys the Special Pass of an individual ticket (same price as the ticket)."),
            new CommandDescriptor("SPECIALPASS DELETE;<specialPassIdentifier>",
                    "Refunds an unconsumed Special Pass of a ticket dated after the current date."),
            new CommandDescriptor("HELP",
                    "Lists the syntax of every command."),
            new CommandDescriptor("HELP EXTENDED",
                    "Lists every command with a brief explanation."),
            new CommandDescriptor("EXIT",
                    "Ends the application; no further commands are processed."));

    private CommandCatalog() {
    }

    public static List<CommandDescriptor> all() {
        return COMMANDS;
    }
}
