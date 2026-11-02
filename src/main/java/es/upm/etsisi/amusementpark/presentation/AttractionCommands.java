package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionController;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.attraction.Entry;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.user.User;
import java.util.List;
import java.util.Set;

/**
 * ATTRACTION CREATE / ENTER / EXIT / LIST / STATUS.
 */
public final class AttractionCommands {

    private static final String SPEED = "SPEED";
    private static final String NL = System.lineSeparator();

    private AttractionCommands() {
    }

    public static void register(CommandRegistry registry, AttractionController attractions) {
        registry.register(new CommandSpec("ATTRACTION CREATE",
                List.of("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                        "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>"),
                "Creates an attraction; type is GENERAL, INDIVIDUAL, GROUP or SPECIAL (GENERAL when omitted).",
                Set.of(4, 5), p -> {
                    AttractionType type = p.length == 5 ? AttractionType.fromName(p[4]).orElseThrow(
                            () -> new CommandSyntaxException("unknown attraction type '" + p[4] + "'"))
                            : AttractionType.GENERAL;
                    attractions.create(p[1], p[2], Args.integer(p[3], "capacity"), type);
                    return CommandResult.message("ATTRACTION CREATED");
                }));
        registry.register(new CommandSpec("ATTRACTION ENTER",
                List.of("ATTRACTION ENTER;<attraction>;<ticket>", "ATTRACTION ENTER;<attraction>;<ticket>;SPEED"),
                "Lets the ticket holders in, all or none; SPEED consumes one SpeedPass each, SPECIAL attractions "
                        + "consume the Special Pass automatically.",
                Set.of(3, 4), p -> {
                    if (p.length == 4 && !SPEED.equals(p[3])) {
                        throw new CommandSyntaxException("unknown entry option '" + p[3] + "', expected SPEED");
                    }
                    Entry entry = attractions.enter(p[1], p[2], p.length == 4);
                    return CommandResult.message("ENTERED " + p[1] + ": " + emails(entry.entrants())
                            + consumed(entry.passes()));
                }));
        registry.register(CommandSpec.of("ATTRACTION EXIT;<attraction>;<email>",
                "Lets one user out of an attraction, releasing one place.", p -> {
                    attractions.exit(p[1], p[2]);
                    return CommandResult.message("USER LEFT " + p[1]);
                }));
        registry.register(CommandSpec.of("ATTRACTION LIST;<attraction>",
                "Lists the emails of the users currently inside an attraction.", p -> {
                    List<User> occupants = attractions.require(p[1]).getOccupants();
                    if (occupants.isEmpty()) {
                        return CommandResult.message("NO USERS IN " + p[1]);
                    }
                    return CommandResult.message(String.join(NL, occupants.stream().map(User::getEmail).toList()));
                }));
        registry.register(CommandSpec.of("ATTRACTION STATUS;<attractionIdentifier>",
                "Shows type, capacity, occupied and free places and the users inside an attraction.", p -> {
                    Attraction<?> a = attractions.require(p[1]);
                    return CommandResult.message(String.join(NL,
                            "ATTRACTION: " + a.getId(),
                            "NAME: " + a.getName(),
                            "TYPE: " + a.getType(),
                            "CAPACITY: " + a.getCapacity(),
                            "INSIDE: " + a.getOccupants().size(),
                            "AVAILABLE: " + a.getFreePlaces(),
                            "USERS: " + emails(a.getOccupants())));
                }));
    }

    private static String emails(List<User> users) {
        return users.isEmpty() ? "-" : String.join(", ", users.stream().map(User::getEmail).toList());
    }

    private static String consumed(List<Pass> passes) {
        if (passes.isEmpty()) {
            return "";
        }
        String kind = passes.get(0) instanceof SpecialPass ? "SPECIAL PASS" : "SPEEDPASS";
        return "; " + kind + " CONSUMED: " + String.join(", ", passes.stream().map(Pass::getId).toList());
    }
}
