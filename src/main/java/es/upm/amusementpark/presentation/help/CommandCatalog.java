package es.upm.amusementpark.presentation.help;

import java.util.List;

/**
 * Ordered, immutable list of the commands documented by help.
 */
public final class CommandCatalog {

    private final List<CommandDescription> commands;

    public CommandCatalog(List<CommandDescription> commands) {
        this.commands = List.copyOf(commands);
    }

    public List<CommandDescription> commands() {
        return commands;
    }

    /** @return the catalog of the application's commands, in the mandated help order */
    public static CommandCatalog standard() {
        return new CommandCatalog(List.of(
                new CommandDescription("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                        "Registers a new user."),
                new CommandDescription("USER DELETE;<Email>;<Password>", "Deletes a user after checking the password."),
                new CommandDescription("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                        "Changes one field of a user after checking the password."),
                new CommandDescription("SYSTEM DATE;<yyyy-MM-dd>", "Sets the current system date."),
                new CommandDescription("SYSTEM PRICE;<Price>", "Sets the base ticket price for new tickets."),
                new CommandDescription("GROUP CREATE;<Identifier>;<Name>", "Creates an empty group."),
                new CommandDescription("GROUP ADD;<Identifier>;<Email>", "Adds an existing user to a group."),
                new CommandDescription("GROUP REMOVE;<Identifier>;<Email>", "Removes a user from a group."),
                new CommandDescription("GROUP DELETE;<Identifier>", "Deletes a group without active group tickets."),
                new CommandDescription("TICKET CREATE;<identifier>;<email>;<date>",
                        "Buys an individual ticket for a user and date at the current price."),
                new CommandDescription("TICKET DELETE;<identifier>", "Deletes a future individual ticket."),
                new CommandDescription("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                        "Buys a discounted ticket for every current member of a group."),
                new CommandDescription("GROUPTICKET DELETE;<identifier>",
                        "Deletes a future group ticket and its member tickets."),
                new CommandDescription("ATTRACTION CREATE;<identifier>;<name>;<capacity>", "Creates an attraction."),
                new CommandDescription("ATTRACTION ENTER;<attraction>;<ticket>",
                        "Admits the users of a ticket valid today, if there is room for all of them."),
                new CommandDescription("ATTRACTION EXIT;<attraction>;<email>", "A user leaves an attraction."),
                new CommandDescription("ATTRACTION LIST;<attraction>", "Lists the users inside an attraction."),
                new CommandDescription("ATTRACTION STATUS;<attractionIdentifier>",
                        "Shows capacity, occupancy and users of an attraction."),
                new CommandDescription("INVOICE CREATE;USER;<email>", "Shows the invoice of a user."),
                new CommandDescription("INVOICE CREATE;GROUP;<groupIdentifier>", "Shows the invoice of a group."),
                new CommandDescription("HELP", "Lists the commands and their syntax."),
                new CommandDescription("HELP EXTENDED", "Lists the commands with their syntax and purpose."),
                new CommandDescription("EXIT", "Ends the application; later commands are not processed.")));
    }
}
