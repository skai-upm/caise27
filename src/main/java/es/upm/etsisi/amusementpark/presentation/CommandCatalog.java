package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Every command form the application recognizes, in the order help shows them.
 */
public final class CommandCatalog {
    public static final List<CommandSpec> ALL = List.of(
            new CommandSpec("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                    "Registers a user after validating DNI, email, password and card; DNI and email must be unique."),
            new CommandSpec("USER DELETE;<Email>;<Password>",
                    "Deletes a user after checking the password, unless active tickets or locked groups prevent it."),
            new CommandSpec("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                    "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD after checking the password."),
            new CommandSpec("SYSTEM DATE;<yyyy-MM-dd>", "Sets the current system date used by every date rule."),
            new CommandSpec("SYSTEM PRICE;<Price>", "Sets the base price applied to tickets created from now on."),
            new CommandSpec("GROUP CREATE;<Identifier>;<Name>", "Creates an empty group."),
            new CommandSpec("GROUP ADD;<Identifier>;<Email>", "Adds a registered user to an unlocked group."),
            new CommandSpec("GROUP REMOVE;<Identifier>;<Email>", "Removes a member from an unlocked group."),
            new CommandSpec("GROUP DELETE;<Identifier>", "Deletes a group with no active group ticket."),
            new CommandSpec("TICKET CREATE;<identifier>;<email>;<date>",
                    "Buys an individual ticket for a user at the current base price; shows the price."),
            new CommandSpec("TICKET DELETE;<identifier>",
                    "Refunds a future standalone ticket together with its passes."),
            new CommandSpec("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                    "Buys a discounted ticket for every member of a group; shows the total."),
            new CommandSpec("GROUPTICKET DELETE;<identifier>",
                    "Refunds a future group ticket, its composition tickets and their passes."),
            new CommandSpec("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                    "Creates a GENERAL attraction with the given capacity."),
            new CommandSpec("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                    "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL."),
            new CommandSpec("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Admits the ticket's users if it is valid today, accepted by the attraction and places are free; "
                            + "SPECIAL attractions consume the ticket's Special Pass."),
            new CommandSpec("ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                    "Enters using one unused SpeedPass per entering ticket (not at SPECIAL attractions)."),
            new CommandSpec("ATTRACTION EXIT;<attraction>;<email>", "Lets one user leave an attraction."),
            new CommandSpec("ATTRACTION LIST;<attraction>", "Lists the emails of the users inside an attraction."),
            new CommandSpec("ATTRACTION STATUS;<attractionIdentifier>",
                    "Shows an attraction's type, capacity, occupancy, free places and users inside."),
            new CommandSpec("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                    "Buys 1 or 10 SpeedPasses for an individual ticket at 20% of its price each; shows the total."),
            new CommandSpec("SPEEDPASS DELETE;<speedPassIdentifier>",
                    "Refunds an unused SpeedPass whose ticket is for a future date."),
            new CommandSpec("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                    "Buys the single Special Pass of an individual ticket at the ticket's price."),
            new CommandSpec("SPECIALPASS DELETE;<specialPassIdentifier>",
                    "Refunds an unused Special Pass whose ticket is for a future date."),
            new CommandSpec("INVOICE CREATE;USER;<email>",
                    "Shows a user's invoice: direct tickets and personal passes with their total."),
            new CommandSpec("INVOICE CREATE;GROUP;<groupIdentifier>",
                    "Shows a group's invoice: its group tickets and their composition with the total."),
            new CommandSpec("HELP", "Lists the syntax of every command."),
            new CommandSpec("HELP EXTENDED", "Lists every command with a short explanation."),
            new CommandSpec("EXIT", "Ends the application; later commands in a file are not run."));

    private CommandCatalog() {
    }
}
