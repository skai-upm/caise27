package es.upm.amusementpark.presentation;

import java.util.List;

/** The commands recognized by the application, in help order. */
public class CommandCatalog {

    private final List<CommandInfo> commands;

    public CommandCatalog(List<CommandInfo> commands) {
        this.commands = List.copyOf(commands);
    }

    public List<CommandInfo> commands() {
        return commands;
    }

    /** Catalog of every command of the application. */
    public static CommandCatalog standard() {
        return new CommandCatalog(List.of(
                new CommandInfo("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                        "Registers a new user with valid personal, contact and payment data."),
                new CommandInfo("USER DELETE;<Email>;<Password>",
                        "Deletes a user after checking the password, if no active ticket or locked group prevents it."),
                new CommandInfo("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                        "Changes one field of a user after checking the password."),
                new CommandInfo("SYSTEM DATE;<yyyy-MM-dd>", "Sets the current system date used by date-dependent operations."),
                new CommandInfo("SYSTEM PRICE;<Price>", "Sets the base ticket price for new tickets in this execution."),
                new CommandInfo("GROUP CREATE;<Identifier>;<Name>", "Creates an empty group of users."),
                new CommandInfo("GROUP ADD;<Identifier>;<Email>",
                        "Adds an existing user to a group without an active group ticket."),
                new CommandInfo("GROUP REMOVE;<Identifier>;<Email>",
                        "Removes a user from a group without an active group ticket."),
                new CommandInfo("GROUP DELETE;<Identifier>",
                        "Deletes a group that has no group ticket for today or later."),
                new CommandInfo("TICKET CREATE;<identifier>;<email>;<date>",
                        "Buys an individual ticket for a user and date at the current price."),
                new CommandInfo("TICKET DELETE;<identifier>", "Deletes an individual ticket dated after today."),
                new CommandInfo("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                        "Buys a discounted ticket for all current members of a group."),
                new CommandInfo("GROUPTICKET DELETE;<identifier>",
                        "Deletes a group ticket dated after today together with its individual tickets."),
                new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                        "Creates an attraction with a maximum capacity."),
                new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>",
                        "Lets the users of a ticket valid today enter an attraction, all or none."),
                new CommandInfo("ATTRACTION EXIT;<attraction>;<email>",
                        "Makes a user leave an attraction, releasing the place."),
                new CommandInfo("ATTRACTION LIST;<attraction>", "Lists the emails of the users inside an attraction."),
                new CommandInfo("ATTRACTION STATUS;<attractionIdentifier>",
                        "Shows capacity, occupancy, free places and occupants of an attraction."),
                new CommandInfo("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                        "Buys 1 or 10 SpeedPasses for an individual ticket, each at 20% of the ticket price."),
                new CommandInfo("SPEEDPASS DELETE;<speedPassIdentifier>",
                        "Refunds an unconsumed SpeedPass whose ticket is dated after today."),
                new CommandInfo("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                        "Buys the single Special Pass of an individual ticket, at the ticket price."),
                new CommandInfo("SPECIALPASS DELETE;<specialPassIdentifier>",
                        "Refunds an unconsumed Special Pass whose ticket is dated after today."),
                new CommandInfo("INVOICE CREATE;USER;<email>", "Shows the invoice of a user's own purchases."),
                new CommandInfo("INVOICE CREATE;GROUP;<groupIdentifier>",
                        "Shows the invoice of the group tickets bought by a group."),
                new CommandInfo("HELP", "Lists the syntax of the available commands."),
                new CommandInfo("HELP EXTENDED", "Lists the available commands with a short explanation of each."),
                new CommandInfo("EXIT", "Ends the application; no later command is processed.")));
    }
}
