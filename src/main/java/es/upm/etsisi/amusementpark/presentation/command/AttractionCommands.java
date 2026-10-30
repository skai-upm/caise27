package es.upm.etsisi.amusementpark.presentation.command;

import es.upm.etsisi.amusementpark.business.controller.AttractionController;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.EntryResult;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.presentation.CommandSyntaxException;

import java.util.List;

/**
 * ATTRACTION CREATE / ENTER / EXIT / LIST / STATUS.
 */
public class AttractionCommands implements CommandModule {

    private static final String SPEED = "SPEED";

    private final AttractionController attractions;

    public AttractionCommands(AttractionController attractions) {
        this.attractions = attractions;
    }

    @Override
    public void registerIn(CommandRegistry registry) {
        registry.register("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                "Creates a GENERAL attraction with the given capacity.",
                p -> created(attractions.create(p[1], p[2], p[3])));
        registry.register("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL.",
                p -> created(attractions.create(p[1], p[2], p[3], p[4])));
        registry.register("ATTRACTION ENTER;<attraction>;<ticket>",
                "Lets the ticket's users in if valid today and accepted; SPECIAL attractions use the Special Pass.",
                p -> entered(attractions.enter(p[1], p[2], false)));
        registry.register("ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                "Same as ATTRACTION ENTER, consuming one SpeedPass per entering ticket (not at SPECIAL attractions).",
                p -> {
                    if (!SPEED.equals(p[3].trim())) {
                        throw new CommandSyntaxException("Fourth field must be SPEED");
                    }
                    return entered(attractions.enter(p[1], p[2], true));
                });
        registry.register("ATTRACTION EXIT;<attraction>;<email>",
                "Removes a user from an attraction, freeing one place.",
                p -> {
                    attractions.exit(p[1], p[2]);
                    return "ATTRACTION EXITED";
                });
        registry.register("ATTRACTION LIST;<attraction>",
                "Lists the emails of the users currently inside an attraction.",
                p -> listOccupants(attractions.find(p[1])));
        registry.register("ATTRACTION STATUS;<attractionIdentifier>",
                "Shows identifier, name, type, capacity, occupancy, free places and users inside.",
                p -> status(attractions.find(p[1])));
    }

    private static String created(Attraction<?> attraction) {
        return "ATTRACTION CREATED (" + attraction.getType() + ")";
    }

    private static String entered(EntryResult result) {
        StringBuilder text = new StringBuilder("ATTRACTION ENTERED: " + result.entered().size() + " user(s)");
        List<Pass> passes = result.consumedPasses();
        if (!passes.isEmpty()) {
            String kind = passes.get(0) instanceof SpecialPass ? "SPECIALPASS" : "SPEEDPASS";
            text.append(System.lineSeparator()).append(kind).append(" CONSUMED: ")
                    .append(String.join(", ", passes.stream().map(Pass::getId).toList()));
        }
        return text.toString();
    }

    private static String listOccupants(Attraction<?> attraction) {
        List<String> emails = attraction.getOccupants().stream().map(User::getEmail).toList();
        return emails.isEmpty() ? "NO USERS INSIDE" : String.join(System.lineSeparator(), emails);
    }

    private static String status(Attraction<?> a) {
        return String.join(System.lineSeparator(),
                "ATTRACTION: " + a.getId(),
                "NAME: " + a.getName(),
                "TYPE: " + a.getType(),
                "CAPACITY: " + a.getCapacity(),
                "INSIDE: " + a.getOccupants().size(),
                "AVAILABLE: " + a.getAvailablePlaces(),
                "USERS: " + String.join(", ", a.getOccupants().stream().map(User::getEmail).toList()));
    }
}
