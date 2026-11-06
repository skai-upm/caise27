package es.upm.park.presentation.help;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import es.upm.park.business.model.AttractionType;
import es.upm.park.business.model.UserField;

/**
 * The commands recognized by the application, in help order.
 */
public final class CommandCatalog {

    private static final List<CommandInfo> COMMANDS = List.of(
            CommandInfo.of("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
                    "Registers a new user after validating the data and the uniqueness of DNI and email."),
            CommandInfo.of("USER DELETE;<email>;<password>",
                    "Deletes a user identified by email and password if no active ticket or locked group prevents it."),
            CommandInfo.of("USER UPDATE;<email>;<password>;<field>;<value>",
                    "Changes one modifiable field of an authenticated user; the DNI never changes.",
                    "<field>: " + Arrays.stream(UserField.values()).map(Enum::name)
                            .collect(Collectors.joining(", "))),
            CommandInfo.of("GROUP CREATE;<id>;<name>", "Creates an empty group of users."),
            CommandInfo.of("GROUP ADD;<id>;<email>", "Adds an existing user to a group that has no active group ticket."),
            CommandInfo.of("GROUP REMOVE;<id>;<email>", "Removes a user from a group that has no active group ticket."),
            CommandInfo.of("GROUP DELETE;<id>", "Deletes a group that has no active group ticket."),
            CommandInfo.of("TICKET CREATE;<id>;<email>;<yyyy-MM-dd>",
                    "Buys an individual ticket for a user at the current base price."),
            CommandInfo.of("TICKET DELETE;<id>", "Deletes an individual ticket dated after the current date."),
            CommandInfo.of("GROUPTICKET CREATE;<id>;<group>;<yyyy-MM-dd>",
                    "Buys a discounted ticket for every current member of a group."),
            CommandInfo.of("GROUPTICKET DELETE;<id>", "Deletes a group ticket dated after the current date."),
            CommandInfo.of("SPEEDPASS CREATE;<purchase>;<ticket>;<quantity>",
                    "Buys 1 or 10 SpeedPasses for an individual ticket, each at 20% of its price."),
            CommandInfo.of("SPEEDPASS DELETE;<pass>", "Refunds an unconsumed SpeedPass of a ticket dated after today."),
            CommandInfo.of("SPECIALPASS CREATE;<id>;<ticket>",
                    "Buys the single Special Pass of an individual ticket, at the ticket's price."),
            CommandInfo.of("SPECIALPASS DELETE;<id>", "Refunds an unconsumed Special Pass of a ticket dated after today."),
            CommandInfo.of("ATTRACTION CREATE;<id>;<name>;<capacity>[;<type>]",
                    "Creates an attraction with a maximum capacity and access type.",
                    "<type>: " + Arrays.stream(AttractionType.values()).map(Enum::name)
                            .collect(Collectors.joining(", ")) + " (default GENERAL)"),
            CommandInfo.of("ATTRACTION LIST;<id>", "Lists the emails of the users inside an attraction."),
            CommandInfo.of("ATTRACTION ENTER;<attraction>;<ticket>",
                    "Lets the users of a ticket valid today into an attraction, all or none."),
            CommandInfo.of("ATTRACTION EXIT;<attraction>;<email>", "Makes a user leave an attraction."),
            CommandInfo.of("ATTRACTION STATUS;<id>", "Shows capacity, occupancy and users of an attraction."),
            CommandInfo.of("INVOICE CREATE;USER;<email>", "Prints the invoice of a user's own purchases."),
            CommandInfo.of("INVOICE CREATE;GROUP;<group>", "Prints the invoice of a group's group tickets."),
            CommandInfo.of("SYSTEM DATE;<yyyy-MM-dd>", "Sets the current system date."),
            CommandInfo.of("SYSTEM PRICE;<price>", "Sets the base ticket price for this execution."),
            CommandInfo.of("HELP", "Lists the syntax of every command."),
            CommandInfo.of("HELP EXTENDED", "Lists every command with its syntax and purpose."),
            CommandInfo.of("EXIT", "Ends the application; no further command is processed."));

    private CommandCatalog() {
    }

    public static List<CommandInfo> commands() {
        return COMMANDS;
    }
}
