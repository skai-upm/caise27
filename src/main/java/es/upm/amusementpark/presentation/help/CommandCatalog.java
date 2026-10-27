package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * The commands recognised by the application, in help order.
 */
public final class CommandCatalog {

    private static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                    "Registers a new user after validating DNI, email, password and card."),
            new CommandInfo("USER DELETE;<Email>;<Password>",
                    "Deletes an identified user who has no active tickets nor belongs to a locked group."),
            new CommandInfo("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                    "Changes one field (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) of an identified user."),
            new CommandInfo("SYSTEM DATE;<yyyy-MM-dd>", "Sets the current system date used by date-dependent operations."),
            new CommandInfo("SYSTEM PRICE;<Price>", "Sets the base price of tickets created from now on."),
            new CommandInfo("GROUP CREATE;<Identifier>;<Name>", "Creates an empty group of users."),
            new CommandInfo("GROUP ADD;<Identifier>;<Email>", "Adds a registered user to a modifiable group."),
            new CommandInfo("GROUP REMOVE;<Identifier>;<Email>", "Removes a member from a modifiable group."),
            new CommandInfo("GROUP DELETE;<Identifier>", "Deletes a group without active group tickets."),
            new CommandInfo("TICKET CREATE;<identifier>;<email>;<date>",
                    "Buys an individual ticket for a user at the current base price."),
            new CommandInfo("TICKET DELETE;<identifier>", "Deletes an individual ticket dated after the current date."),
            new CommandInfo("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                    "Buys a joint ticket for all current members of a group, with a size discount."),
            new CommandInfo("GROUPTICKET DELETE;<identifier>",
                    "Deletes a group ticket dated after the current date, with its member tickets."),
            new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                    "Creates an attraction with a maximum capacity."),
            new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Lets the users of a ticket valid today enter an attraction, all together or none."),
            new CommandInfo("ATTRACTION EXIT;<attraction>;<email>", "A user leaves an attraction, freeing a place."),
            new CommandInfo("ATTRACTION LIST;<attraction>", "Lists the emails of the users inside an attraction."),
            new CommandInfo("ATTRACTION STATUS;<attractionIdentifier>",
                    "Shows capacity, occupancy, free places and users of an attraction."),
            new CommandInfo("INVOICE CREATE;USER;<email>", "Shows the invoice of the purchases of a user."),
            new CommandInfo("INVOICE CREATE;GROUP;<groupIdentifier>", "Shows the invoice of the group tickets of a group."),
            new CommandInfo("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                    "Buys 1 or 10 SpeedPasses (20% of the ticket price each) for an individual ticket."),
            new CommandInfo("SPEEDPASS DELETE;<speedPassIdentifier>",
                    "Refunds an unconsumed SpeedPass of a ticket dated after the current date."),
            new CommandInfo("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                    "Buys the single Special Pass of an individual ticket (same price as the ticket)."),
            new CommandInfo("SPECIALPASS DELETE;<specialPassIdentifier>",
                    "Refunds an unconsumed Special Pass of a ticket dated after the current date."),
            new CommandInfo("HELP", "Lists the syntax of the available commands."),
            new CommandInfo("HELP EXTENDED", "Lists the available commands with a brief explanation."),
            new CommandInfo("EXIT", "Ends the application; no further commands are executed."));

    public List<CommandInfo> commands() {
        return COMMANDS;
    }
}
