package es.upm.etsisi.amusementpark.presentation;

import java.util.List;

/**
 * Syntax and purpose of every command recognised by the application, used to
 * generate help in any format.
 */
public final class CommandCatalog {

    /** One recognised command. */
    public record CommandInfo(String syntax, String purpose) {
    }

    private final List<CommandInfo> commands;

    public CommandCatalog(List<CommandInfo> commands) {
        this.commands = List.copyOf(commands);
    }

    public List<CommandInfo> getCommands() {
        return commands;
    }

    /** The commands of Deliveries 1 to 3. */
    public static CommandCatalog standard() {
        return new CommandCatalog(List.of(
                info("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                        "Registers a user after validating DNI, email, password and card; DNI and email must be unique."),
                info("USER DELETE;<Email>;<Password>",
                        "Deletes a user after checking the password, unless they hold active tickets or belong to a locked group."),
                info("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                        "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD after checking the password; DNI cannot change."),
                info("SYSTEM DATE;<yyyy-MM-dd>", "Sets the current system date used by date-dependent operations."),
                info("SYSTEM PRICE;<Price>", "Sets the base price of tickets created from now on in this execution."),
                info("GROUP CREATE;<Identifier>;<Name>", "Creates an empty group with a unique identifier."),
                info("GROUP ADD;<Identifier>;<Email>", "Adds a registered user to a group without active group tickets."),
                info("GROUP REMOVE;<Identifier>;<Email>", "Removes a member from a group without active group tickets."),
                info("GROUP DELETE;<Identifier>", "Deletes a group without active group tickets; historical tickets are kept."),
                info("TICKET CREATE;<identifier>;<email>;<date>",
                        "Buys an individual ticket for a user, dated today or later, at the current price."),
                info("TICKET DELETE;<identifier>",
                        "Refunds a standalone individual ticket dated after today, together with its passes."),
                info("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                        "Buys a discounted group ticket with one ticket <identifier>:<dni> per current member."),
                info("GROUPTICKET DELETE;<identifier>",
                        "Refunds a group ticket dated after today with its composed tickets and their passes."),
                info("ATTRACTION CREATE;<identifier>;<name>;<capacity>", "Creates a GENERAL attraction."),
                info("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                        "Creates a GENERAL, INDIVIDUAL, GROUP or SPECIAL attraction."),
                info("ATTRACTION ENTER;<attraction>;<ticket>",
                        "Lets the ticket's users in if the ticket is valid today, accepted and there is room."),
                info("ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                        "Enters using one unused SpeedPass per entering ticket; not allowed at SPECIAL attractions."),
                info("ATTRACTION EXIT;<attraction>;<email>", "Lets a user leave an attraction, releasing one place."),
                info("ATTRACTION LIST;<attraction>", "Lists the emails of the users currently inside an attraction."),
                info("ATTRACTION STATUS;<attractionIdentifier>",
                        "Shows type, capacity, occupied and available places and users inside."),
                info("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                        "Buys 1 or 10 SpeedPasses <purchaseIdentifier>:<n> at 20% of the ticket price each."),
                info("SPEEDPASS DELETE;<speedPassIdentifier>",
                        "Refunds an unused SpeedPass of a ticket dated after today."),
                info("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                        "Buys the ticket's only Special Pass at the ticket price; needed for SPECIAL attractions."),
                info("SPECIALPASS DELETE;<specialPassIdentifier>",
                        "Refunds an unused Special Pass of a ticket dated after today."),
                info("INVOICE CREATE;USER;<email>",
                        "Shows a user's invoice: direct tickets and personal passes with their total."),
                info("INVOICE CREATE;GROUP;<groupIdentifier>",
                        "Shows a group's invoice: its group tickets and composition with their total."),
                info("HELP", "Lists the syntax of every command."),
                info("HELP EXTENDED", "Lists every command with its syntax and purpose."),
                info("EXIT", "Ends the application; no further commands are processed.")));
    }

    private static CommandInfo info(String syntax, String purpose) {
        return new CommandInfo(syntax, purpose);
    }
}
