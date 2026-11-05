package es.upm.parque.presentation;

import java.util.List;

/**
 * The commands recognized by the application, in the order shown by every help variant.
 */
public final class CommandCatalog {

    public static final List<CommandDescriptor> COMMANDS = List.of(
            new CommandDescriptor("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                    "Registers a new user after validating every field; DNI and email must be unique."),
            new CommandDescriptor("USER DELETE;<Email>;<Password>",
                    "Deletes a user after checking the password, unless the user has active tickets or a locked group."),
            new CommandDescriptor("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                    "Changes one field of a user (NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD); the DNI cannot change."),
            new CommandDescriptor("SYSTEM DATE;<yyyy-MM-dd>",
                    "Sets the current system date used to validate tickets and accesses."),
            new CommandDescriptor("SYSTEM PRICE;<Price>",
                    "Sets the base price applied to tickets created from now on."),
            new CommandDescriptor("GROUP CREATE;<Identifier>;<Name>", "Creates an empty user group."),
            new CommandDescriptor("GROUP ADD;<Identifier>;<Email>",
                    "Adds a registered user to a group without an active group ticket."),
            new CommandDescriptor("GROUP REMOVE;<Identifier>;<Email>",
                    "Removes a member from a group without an active group ticket."),
            new CommandDescriptor("GROUP DELETE;<Identifier>",
                    "Deletes a group that has no group ticket for today or a later date."),
            new CommandDescriptor("TICKET CREATE;<identifier>;<email>;<date>",
                    "Buys an individual ticket for a user and date at the current base price."),
            new CommandDescriptor("TICKET DELETE;<identifier>",
                    "Deletes a stand-alone individual ticket dated after the current date, together with its passes."),
            new CommandDescriptor("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                    "Buys a group ticket with one discounted ticket per current group member."),
            new CommandDescriptor("GROUPTICKET DELETE;<identifier>",
                    "Deletes a group ticket dated after the current date, together with its tickets and their passes."),
            new CommandDescriptor("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                    "Buys 1 or 10 SpeedPasses (20% of the ticket price each) for an individual ticket for today or later."),
            new CommandDescriptor("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                    "Buys the only Special Pass of an individual ticket for today or later, at the ticket price."),
            new CommandDescriptor("SPEEDPASS DELETE;<speedPassIdentifier>",
                    "Refunds an unconsumed SpeedPass whose ticket is dated after the current date."),
            new CommandDescriptor("SPECIALPASS DELETE;<specialPassIdentifier>",
                    "Refunds an unconsumed Special Pass whose ticket is dated after the current date."),
            new CommandDescriptor("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                    "Creates an attraction with a maximum number of simultaneous users."),
            new CommandDescriptor("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Lets the holders of a ticket valid today into an attraction, all or none, within its capacity."),
            new CommandDescriptor("ATTRACTION EXIT;<attraction>;<email>",
                    "Registers that a user leaves an attraction."),
            new CommandDescriptor("ATTRACTION LIST;<attraction>", "Lists the users currently inside an attraction."),
            new CommandDescriptor("ATTRACTION STATUS;<attractionIdentifier>",
                    "Shows the occupancy and free places of an attraction."),
            new CommandDescriptor("INVOICE CREATE;USER;<email>",
                    "Generates the invoice of a user with the tickets bought directly for that user."),
            new CommandDescriptor("INVOICE CREATE;GROUP;<groupIdentifier>",
                    "Generates the invoice of a group with its group tickets and their composition."),
            new CommandDescriptor("HELP", "Lists the syntax of every available command."),
            new CommandDescriptor("HELP EXTENDED", "Lists every available command with its syntax and purpose."),
            new CommandDescriptor("EXIT", "Ends the application; no further command is processed."));

    private CommandCatalog() {
    }

    /** The syntax of every command, in help order. */
    public static List<String> syntaxes() {
        return COMMANDS.stream().map(CommandDescriptor::syntax).toList();
    }
}
