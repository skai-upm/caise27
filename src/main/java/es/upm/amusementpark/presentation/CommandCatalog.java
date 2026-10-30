package es.upm.amusementpark.presentation;

import java.util.List;

/**
 * Every command recognised by the application, from the three deliveries.
 */
public final class CommandCatalog {

    private static final List<CommandHelp> COMMANDS = List.of(
            new CommandHelp("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                    "Registers a user after validating DNI, email, password and card."),
            new CommandHelp("USER DELETE;<Email>;<Password>",
                    "Deletes a user without active tickets or locked groups, after checking the password."),
            new CommandHelp("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                    "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of a user after checking the password."),
            new CommandHelp("SYSTEM DATE;<yyyy-MM-dd>", "Sets the current system date used by date-dependent operations."),
            new CommandHelp("SYSTEM PRICE;<Price>", "Sets the base price for tickets created from now on."),
            new CommandHelp("GROUP CREATE;<Identifier>;<Name>", "Creates an empty group of users."),
            new CommandHelp("GROUP ADD;<Identifier>;<Email>", "Adds a registered user to an unlocked group."),
            new CommandHelp("GROUP REMOVE;<Identifier>;<Email>", "Removes a member from an unlocked group."),
            new CommandHelp("GROUP DELETE;<Identifier>",
                    "Deletes a group without active group tickets; historical tickets are kept."),
            new CommandHelp("TICKET CREATE;<identifier>;<email>;<date>",
                    "Buys an individual ticket for a user at the current price, for today or a later date."),
            new CommandHelp("TICKET DELETE;<identifier>",
                    "Refunds a future individual ticket together with its SpeedPasses and Special Pass."),
            new CommandHelp("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                    "Buys a discounted ticket for every member of a non-empty group."),
            new CommandHelp("GROUPTICKET DELETE;<identifier>",
                    "Refunds a future group ticket, its composed tickets and their passes."),
            new CommandHelp("ATTRACTION CREATE;<identifier>;<name>;<capacity>", "Creates a GENERAL attraction."),
            new CommandHelp("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                    "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL."),
            new CommandHelp("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Lets the ticket holders in if the ticket is for today and accepted by the attraction type; "
                            + "SPECIAL attractions consume the ticket's Special Pass."),
            new CommandHelp("ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                    "Enters using one unconsumed SpeedPass per admitted ticket (not at SPECIAL attractions)."),
            new CommandHelp("ATTRACTION EXIT;<attraction>;<email>", "Takes a user out of an attraction."),
            new CommandHelp("ATTRACTION LIST;<attraction>", "Lists the email addresses of the users inside."),
            new CommandHelp("ATTRACTION STATUS;<attractionIdentifier>",
                    "Shows identifier, name, type, capacity, occupancy, free places and users inside."),
            new CommandHelp("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                    "Buys 1 or 10 SpeedPasses (20% of the ticket price each) for a current or future ticket."),
            new CommandHelp("SPEEDPASS DELETE;<speedPassIdentifier>",
                    "Refunds an unconsumed SpeedPass of a future ticket."),
            new CommandHelp("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                    "Buys the ticket's only Special Pass (same price as the ticket)."),
            new CommandHelp("SPECIALPASS DELETE;<specialPassIdentifier>",
                    "Refunds an unconsumed Special Pass of a future ticket."),
            new CommandHelp("INVOICE CREATE;USER;<email>",
                    "Shows a user's invoice: direct tickets, SpeedPasses and Special Passes."),
            new CommandHelp("INVOICE CREATE;GROUP;<groupIdentifier>",
                    "Shows a group's invoice: its group tickets with their composition."),
            new CommandHelp("HELP", "Lists the syntax of every command."),
            new CommandHelp("HELP EXTENDED", "Lists every command with a description of its purpose."),
            new CommandHelp("EXIT", "Ends the application; later commands are not processed."));

    private CommandCatalog() {
    }

    public static List<CommandHelp> commands() {
        return COMMANDS;
    }
}
