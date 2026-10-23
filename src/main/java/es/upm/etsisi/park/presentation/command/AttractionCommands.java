package es.upm.etsisi.park.presentation.command;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.park.business.controller.AttractionController;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.business.model.attraction.AttractionType;
import es.upm.etsisi.park.business.model.attraction.Entry;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.user.User;
import es.upm.etsisi.park.presentation.Args;
import es.upm.etsisi.park.presentation.CommandDefinition;
import es.upm.etsisi.park.presentation.CommandGroup;
import es.upm.etsisi.park.presentation.CommandRegistry;

/** ATTRACTION CREATE, ENTER, EXIT, LIST and STATUS. */
public class AttractionCommands implements CommandGroup {

    static final String CREATE = "ATTRACTION CREATE;<identifier>;<name>;<capacity>";
    static final String CREATE_TYPED = "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>";
    static final String ENTER = "ATTRACTION ENTER;<attraction>;<ticket>";
    static final String ENTER_SPEED = "ATTRACTION ENTER;<attraction>;<ticket>;SPEED";
    static final String EXIT = "ATTRACTION EXIT;<attraction>;<email>";
    static final String LIST = "ATTRACTION LIST;<attraction>";
    static final String STATUS = "ATTRACTION STATUS;<attraction>";

    private final AttractionController attractions;

    public AttractionCommands(AttractionController attractions) {
        this.attractions = attractions;
    }

    @Override
    public void register(CommandRegistry registry) {
        registry.register(CommandDefinition.of("ATTRACTION CREATE",
                "Creates an attraction with a capacity and a type that restricts the accepted tickets (default GENERAL).",
                (args, out) -> {
                    Args.require(args, CREATE_TYPED, 3, 4);
                    attractions.create(args[0], args[1], args[2], Args.optional(args, 3));
                    out.println("ATTRACTION CREATED");
                }, CREATE, CREATE_TYPED).withNotes("<type> is one of: " + AttractionType.names()));
        registry.register(CommandDefinition.of("ATTRACTION ENTER",
                "Lets the holders of a ticket valid today in (all or none); SPEED consumes one SpeedPass per ticket, "
                        + "SPECIAL attractions consume the Special Pass automatically.",
                (args, out) -> {
                    Args.require(args, ENTER_SPEED, 2, 3);
                    Entry entry = attractions.enter(args[0], args[1], Args.optional(args, 2));
                    out.println("ATTRACTION ENTERED: " + entry.users().size() + " user(s) entered "
                            + attractions.require(args[0]).getId());
                    for (Pass pass : entry.consumedPasses()) {
                        out.println(pass.getKind().toUpperCase() + " CONSUMED: " + pass.getId() + " (ticket "
                                + pass.getTicket().getId() + ")");
                    }
                }, ENTER, ENTER_SPEED));
        registry.register(CommandDefinition.of("ATTRACTION EXIT",
                "Takes a user out of an attraction, freeing their place.",
                (args, out) -> {
                    Args.require(args, EXIT, 2);
                    User user = attractions.exit(args[0], args[1]);
                    out.println("ATTRACTION EXITED: " + user.getEmail() + " left " + attractions.require(args[0]).getId());
                }, EXIT));
        registry.register(CommandDefinition.of("ATTRACTION LIST",
                "Shows the emails of the users currently inside an attraction.",
                (args, out) -> {
                    Args.require(args, LIST, 1);
                    list(attractions.require(args[0]), out);
                }, LIST));
        registry.register(CommandDefinition.of("ATTRACTION STATUS",
                "Shows an attraction's identifier, name, type, capacity, occupancy, free places and users inside.",
                (args, out) -> {
                    Args.require(args, STATUS, 1);
                    status(attractions.require(args[0]), out);
                }, STATUS));
    }

    private static void list(Attraction<?> attraction, PrintStream out) {
        if (attraction.getOccupants().isEmpty()) {
            out.println("ATTRACTION " + attraction.getId() + " IS EMPTY");
            return;
        }
        for (User user : attraction.getOccupants()) {
            out.println(user.getEmail());
        }
    }

    private static void status(Attraction<?> attraction, PrintStream out) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        out.println("Attraction: " + attraction.getId());
        out.println("Name: " + attraction.getName());
        out.println("Type: " + attraction.getType());
        out.println("Capacity: " + attraction.getCapacity());
        out.println("Inside: " + attraction.getOccupants().size());
        out.println("Available: " + attraction.getAvailable());
        out.println("Users: " + (emails.isEmpty() ? "none" : String.join(", ", emails)));
    }
}
