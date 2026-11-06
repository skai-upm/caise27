package es.upm.park.presentation.help;

import java.util.List;

/** The commands offered by the application, in the order shown by help. */
public final class CommandCatalog {

    private CommandCatalog() {
    }

    public static List<CommandDescription> standard() {
        return List.of(
                new CommandDescription("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                        "Registers a new user with personal, contact and payment data."),
                new CommandDescription("USER DELETE;<Email>;<Password>",
                        "Deletes a user after checking the password, if no active ticket or locked group prevents it."),
                new CommandDescription("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                        "Changes one field of a user after checking the password."),
                new CommandDescription("SYSTEM DATE;<yyyy-MM-dd>",
                        "Sets the current system date used by all date-dependent operations."),
                new CommandDescription("SYSTEM PRICE;<Price>",
                        "Sets the base ticket price for tickets created from now on in this execution."),
                new CommandDescription("GROUP CREATE;<Identifier>;<Name>",
                        "Creates an empty group of users."),
                new CommandDescription("GROUP ADD;<Identifier>;<Email>",
                        "Adds a registered user to a group without an active group ticket."),
                new CommandDescription("GROUP REMOVE;<Identifier>;<Email>",
                        "Removes a member from a group without an active group ticket."),
                new CommandDescription("GROUP DELETE;<Identifier>",
                        "Deletes a group that has no group ticket for today or later."),
                new CommandDescription("TICKET CREATE;<identifier>;<email>;<date>",
                        "Buys an individual ticket for a user and date at the current base price."),
                new CommandDescription("TICKET DELETE;<identifier>",
                        "Deletes an individual ticket dated after today."),
                new CommandDescription("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                        "Buys a discounted joint ticket for all current members of a group."),
                new CommandDescription("GROUPTICKET DELETE;<identifier>",
                        "Deletes a group ticket dated after today, together with its individual tickets."),
                new CommandDescription("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                        "Creates a GENERAL attraction with a maximum capacity."),
                new CommandDescription("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                        "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL."),
                new CommandDescription("ATTRACTION ENTER;<attraction>;<ticket>",
                        "Lets the users of a ticket valid today enter an attraction that accepts that ticket, if there is room for all."),
                new CommandDescription("ATTRACTION EXIT;<attraction>;<email>",
                        "Lets a user leave an attraction, freeing a place."),
                new CommandDescription("ATTRACTION LIST;<attraction>",
                        "Lists the emails of the users inside an attraction."),
                new CommandDescription("ATTRACTION STATUS;<attractionIdentifier>",
                        "Shows capacity, occupancy, free places and users of an attraction."),
                new CommandDescription("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                        "Buys 1 or 10 SpeedPasses (20% of the ticket price each) for an individual ticket."),
                new CommandDescription("SPEEDPASS DELETE;<speedPassIdentifier>",
                        "Refunds an unconsumed SpeedPass whose ticket is dated after today."),
                new CommandDescription("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                        "Buys the only Special Pass of an individual ticket, at the ticket's price."),
                new CommandDescription("SPECIALPASS DELETE;<specialPassIdentifier>",
                        "Refunds an unconsumed Special Pass whose ticket is dated after today."),
                new CommandDescription("INVOICE CREATE;USER;<email>",
                        "Shows the invoice of a user's direct tickets and passes."),
                new CommandDescription("INVOICE CREATE;GROUP;<groupIdentifier>",
                        "Shows the invoice of the group tickets bought by a group."),
                new CommandDescription("HELP EXTENDED",
                        "Shows every command with its syntax and purpose."),
                new CommandDescription("HELP",
                        "Shows the syntax of every command."),
                new CommandDescription("EXIT",
                        "Ends the application; no later command is executed."));
    }
}
