package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * The commands recognised by the application, with their syntax and purpose.
 */
public class CommandCatalog {

    private static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                    "Registers a new user after validating DNI, email, password and card."),
            new CommandInfo("USER DELETE;<Email>;<Password>",
                    "Deletes a user without active tickets or locked groups, after checking the password."),
            new CommandInfo("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                    "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of a user (the DNI is fixed)."),
            new CommandInfo("SYSTEM DATE;<yyyy-MM-dd>",
                    "Sets the current system date used by date-dependent operations."),
            new CommandInfo("SYSTEM PRICE;<Price>",
                    "Sets the base price for tickets created from now on in this execution."),
            new CommandInfo("GROUP CREATE;<Identifier>;<Name>", "Creates an empty group of users."),
            new CommandInfo("GROUP ADD;<Identifier>;<Email>", "Adds an existing user to an unlocked group."),
            new CommandInfo("GROUP REMOVE;<Identifier>;<Email>", "Removes a user from an unlocked group."),
            new CommandInfo("GROUP DELETE;<Identifier>", "Deletes a group that has no active group ticket."),
            new CommandInfo("TICKET CREATE;<identifier>;<email>;<date>",
                    "Buys an individual ticket for a user at the current price and shows the price."),
            new CommandInfo("TICKET DELETE;<identifier>",
                    "Refunds a future individual ticket together with its passes."),
            new CommandInfo("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                    "Buys a discounted ticket for every current member of a group and shows the total."),
            new CommandInfo("GROUPTICKET DELETE;<identifier>",
                    "Refunds a future group ticket, its member tickets and their passes."),
            new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                    "Creates a GENERAL attraction with the given capacity."),
            new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                    "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL."),
            new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Lets the users of a ticket valid today enter (SPECIAL attractions use the Special Pass)."),
            new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                    "Enters using one unconsumed SpeedPass per admitted ticket."),
            new CommandInfo("ATTRACTION EXIT;<attraction>;<email>", "Makes a user leave an attraction."),
            new CommandInfo("ATTRACTION LIST;<attraction>", "Lists the emails of the users inside an attraction."),
            new CommandInfo("ATTRACTION STATUS;<attractionIdentifier>",
                    "Shows an attraction's type, capacity, occupancy, free places and users inside."),
            new CommandInfo("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                    "Buys 1 or 10 SpeedPasses (20% of the ticket price each) and shows the total."),
            new CommandInfo("SPEEDPASS DELETE;<speedPassIdentifier>",
                    "Refunds an unconsumed SpeedPass of a ticket dated after today."),
            new CommandInfo("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                    "Buys the ticket's Special Pass (same price as the ticket), required by SPECIAL attractions."),
            new CommandInfo("SPECIALPASS DELETE;<specialPassIdentifier>",
                    "Refunds an unconsumed Special Pass of a ticket dated after today."),
            new CommandInfo("INVOICE CREATE;USER;<email>",
                    "Shows a user's invoice: direct tickets, SpeedPasses, Special Passes and total."),
            new CommandInfo("INVOICE CREATE;GROUP;<groupIdentifier>",
                    "Shows a group's invoice: group tickets with their composition and total."),
            new CommandInfo("HELP", "Shows the syntax of every command."),
            new CommandInfo("HELP EXTENDED", "Shows every command with a short description of its purpose."),
            new CommandInfo("EXIT", "Ends the application; no later command is executed."));

    public List<CommandInfo> commands() {
        return COMMANDS;
    }
}
