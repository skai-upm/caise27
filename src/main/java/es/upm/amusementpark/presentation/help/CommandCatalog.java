package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * The commands recognised by the application, in the order help shows them.
 */
public final class CommandCatalog {

    private static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                    "Registers a new user after validating DNI, email, password and card."),
            new CommandInfo("USER DELETE;<Email>;<Password>",
                    "Deletes a user identified by email and password, if no active ticket or locked group prevents it."),
            new CommandInfo("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                    "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of a user; the DNI cannot be changed."),
            new CommandInfo("SYSTEM DATE;<yyyy-MM-dd>",
                    "Sets the current system date used by all date-dependent operations."),
            new CommandInfo("SYSTEM PRICE;<Price>",
                    "Sets the base ticket price for tickets created from now on in this execution."),
            new CommandInfo("GROUP CREATE;<Identifier>;<Name>",
                    "Creates an empty group of users."),
            new CommandInfo("GROUP ADD;<Identifier>;<Email>",
                    "Adds an existing user to a group that has no active group ticket."),
            new CommandInfo("GROUP REMOVE;<Identifier>;<Email>",
                    "Removes a user from a group that has no active group ticket."),
            new CommandInfo("GROUP DELETE;<Identifier>",
                    "Deletes a group that has no group ticket for today or later."),
            new CommandInfo("TICKET CREATE;<identifier>;<email>;<date>",
                    "Buys an individual ticket for a user at the current price, for today or a later date."),
            new CommandInfo("TICKET DELETE;<identifier>",
                    "Deletes an individual ticket dated after today."),
            new CommandInfo("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                    "Buys a group ticket for all current group members, with a discount by group size."),
            new CommandInfo("GROUPTICKET DELETE;<identifier>",
                    "Deletes a group ticket dated after today, together with its individual tickets."),
            new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                    "Creates a GENERAL attraction with a maximum number of simultaneous users."),
            new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                    "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL, which limits the tickets accepted."),
            new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Lets the users of a ticket valid today into an attraction, if there is room for all of them."),
            new CommandInfo("ATTRACTION EXIT;<attraction>;<email>",
                    "Removes a user from an attraction and frees the place."),
            new CommandInfo("ATTRACTION LIST;<attraction>",
                    "Lists the emails of the users currently inside an attraction."),
            new CommandInfo("ATTRACTION STATUS;<attractionIdentifier>",
                    "Shows name, capacity, occupancy, free places and users of an attraction."),
            new CommandInfo("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                    "Buys 1 or 10 SpeedPasses for an individual ticket, each costing 20% of the ticket price."),
            new CommandInfo("SPEEDPASS DELETE;<speedPassIdentifier>",
                    "Refunds an unconsumed SpeedPass whose ticket is dated after today."),
            new CommandInfo("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                    "Buys the only Special Pass of an individual ticket, costing the same as the ticket."),
            new CommandInfo("SPECIALPASS DELETE;<specialPassIdentifier>",
                    "Refunds an unconsumed Special Pass whose ticket is dated after today."),
            new CommandInfo("INVOICE CREATE;USER;<email>",
                    "Shows the invoice of a user's personal purchases."),
            new CommandInfo("INVOICE CREATE;GROUP;<groupIdentifier>",
                    "Shows the invoice of the group tickets bought by a group."),
            new CommandInfo("HELP",
                    "Shows the syntax of every command."),
            new CommandInfo("HELP EXTENDED",
                    "Shows the syntax and purpose of every command."),
            new CommandInfo("EXIT",
                    "Ends the application; later commands are not processed."));

    private CommandCatalog() {
    }

    public static List<CommandInfo> commands() {
        return COMMANDS;
    }
}
