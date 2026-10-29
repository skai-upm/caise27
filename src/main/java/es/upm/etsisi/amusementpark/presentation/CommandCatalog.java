package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Every command the application understands, with its syntax and purpose.
 */
public final class CommandCatalog {

    public static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                    "Registers a new user after validating DNI, email, password and card."),
            new CommandInfo("USER DELETE;<Email>;<Password>",
                    "Deletes a user when the password matches and no active ticket or locked group prevents it."),
            new CommandInfo("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                    "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of a user (the DNI cannot change)."),
            new CommandInfo("SYSTEM DATE;<yyyy-MM-dd>", "Sets the current system date used by date-dependent operations."),
            new CommandInfo("SYSTEM PRICE;<Price>", "Sets the base price for tickets created from now on."),
            new CommandInfo("GROUP CREATE;<Identifier>;<Name>", "Creates an empty group of users."),
            new CommandInfo("GROUP ADD;<Identifier>;<Email>", "Adds a registered user to a group without active group tickets."),
            new CommandInfo("GROUP REMOVE;<Identifier>;<Email>", "Removes a user from a group without active group tickets."),
            new CommandInfo("GROUP DELETE;<Identifier>", "Deletes a group that has no group ticket for today or later."),
            new CommandInfo("TICKET CREATE;<identifier>;<email>;<date>",
                    "Buys an individual ticket for a user at the current price and shows its price."),
            new CommandInfo("TICKET DELETE;<identifier>",
                    "Refunds a future individual ticket together with its passes."),
            new CommandInfo("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                    "Buys a group ticket for all current members, applying the group discount, and shows the total."),
            new CommandInfo("GROUPTICKET DELETE;<identifier>",
                    "Refunds a future group ticket, its member tickets and their passes."),
            new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>", "Creates a GENERAL attraction."),
            new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                    "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL."),
            new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Lets the users of a ticket valid today enter (special attractions use the Special Pass)."),
            new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                    "Enters consuming one SpeedPass per entering ticket (not at special attractions)."),
            new CommandInfo("ATTRACTION EXIT;<attraction>;<email>", "Makes a user leave an attraction."),
            new CommandInfo("ATTRACTION LIST;<attraction>", "Lists the emails of the users inside an attraction."),
            new CommandInfo("ATTRACTION STATUS;<attractionIdentifier>",
                    "Shows identifier, name, type, capacity, occupation, free places and users of an attraction."),
            new CommandInfo("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                    "Buys 1 or 10 SpeedPasses for an individual ticket and shows the total price."),
            new CommandInfo("SPEEDPASS DELETE;<speedPassIdentifier>",
                    "Refunds an unconsumed SpeedPass of a future ticket."),
            new CommandInfo("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                    "Buys the Special Pass of an individual ticket and shows its price."),
            new CommandInfo("SPECIALPASS DELETE;<specialPassIdentifier>",
                    "Refunds an unconsumed Special Pass of a future ticket."),
            new CommandInfo("INVOICE CREATE;USER;<email>",
                    "Shows the invoice of a user: individual tickets and personal passes."),
            new CommandInfo("INVOICE CREATE;GROUP;<groupIdentifier>",
                    "Shows the invoice of a group: its group tickets and their composition."),
            new CommandInfo("HELP", "Shows the syntax of every command."),
            new CommandInfo("HELP EXTENDED", "Shows the syntax and purpose of every command."),
            new CommandInfo("EXIT", "Ends the application; no further commands are processed."));

    private CommandCatalog() {
    }
}
