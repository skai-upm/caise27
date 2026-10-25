package es.upm.parque.presentation.command;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.User;
import es.upm.parque.presentation.CommandRegistry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ATTRACTION commands.
 */
public final class AttractionCommands {

    private AttractionCommands() {
    }

    public static void register(CommandRegistry registry, AttractionController attractions) {
        registry.register("ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad",
                "Creates a GENERAL attraction, which admits individual and group tickets.",
                p -> {
                    attractions.create(p[1], p[2], p[3], null);
                    return "OK ATTRACTION CREATED";
                });
        registry.register("ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad;Tipo",
                "Creates an attraction of type GENERAL, INDIVIDUAL (individual tickets), GROUP (group tickets) "
                        + "or SPECIAL (individual tickets with a Special Pass).",
                p -> {
                    attractions.create(p[1], p[2], p[3], p[4]);
                    return "OK ATTRACTION CREATED";
                });
        registry.register("ATTRACTION DELETE;IdAtraccion", "Deletes an empty attraction.",
                p -> {
                    attractions.delete(p[1]);
                    return "OK ATTRACTION DELETED";
                });
        registry.register("ATTRACTION ENTER;IdAtraccion;IdEntrada",
                "Admits the users of a ticket for today if the type admits it and all fit; "
                        + "SPECIAL attractions consume the ticket's Special Pass.",
                p -> entered(attractions.enter(p[1], p[2], null)));
        registry.register("ATTRACTION ENTER;IdAtraccion;IdEntrada;SPEED",
                "Like ATTRACTION ENTER but consumes one unused SpeedPass per admitted ticket (all or nothing).",
                p -> entered(attractions.enter(p[1], p[2], p[3])));
        registry.register("ATTRACTION EXIT;IdAtraccion;IdEntrada", "The users of the ticket leave the attraction.",
                p -> "OK ATTRACTION EXIT -> " + attractions.exit(p[1], p[2]));
        registry.register("ATTRACTION STATUS;IdAtraccion",
                "Shows type, capacity, occupancy, available places and the users present.",
                p -> status(attractions.get(p[1])));
    }

    private static String entered(AttractionController.EntryResult result) {
        StringBuilder text = new StringBuilder("OK ATTRACTION ENTER -> ").append(result.entered());
        List<Pass> consumed = result.consumedPasses();
        if (!consumed.isEmpty()) {
            text.append(" (").append(consumed.stream()
                    .collect(Collectors.groupingBy(Pass::getKind, Collectors.mapping(Pass::getId, Collectors.joining(", "))))
                    .entrySet().stream()
                    .map(entry -> entry.getKey().getDisplayName() + " " + entry.getValue())
                    .collect(Collectors.joining("; "))).append(" consumed)");
        }
        return text.toString();
    }

    private static String status(Attraction<?> attraction) {
        List<User> present = attraction.getPresent();
        StringBuilder text = new StringBuilder()
                .append("ATTRACTION ").append(attraction.getId()).append(" - ").append(attraction.getName())
                .append(System.lineSeparator()).append("  Type: ").append(attraction.getType())
                .append(System.lineSeparator()).append("  Capacity: ").append(attraction.getCapacity())
                .append(System.lineSeparator()).append("  Present: ").append(present.size())
                .append(System.lineSeparator()).append("  Available: ").append(attraction.getAvailable())
                .append(System.lineSeparator()).append("  Users present:");
        if (present.isEmpty()) {
            text.append(" none");
        }
        for (User user : present) {
            text.append(System.lineSeparator()).append("    - ").append(user.getName()).append(' ')
                    .append(user.getSurnames()).append(" (").append(user.getDni()).append(", ")
                    .append(user.getEmail()).append(')');
        }
        return text.toString();
    }
}
