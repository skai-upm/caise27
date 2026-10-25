package es.upm.parque.presentation;

import java.util.List;
import java.util.stream.Collectors;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.EntryResult;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.User;

/**
 * ATTRACTION commands.
 */
public class AttractionCommands implements CommandSet {

    private final AttractionController attractions;

    public AttractionCommands(AttractionController attractions) {
        this.attractions = attractions;
    }

    @Override
    public void registerInto(CommandRegistry registry) {
        registry.register("ATTRACTION CREATE", "IdAtraccion;Nombre;Capacidad[;Tipo]",
                "Creates an attraction; Tipo is GENERAL (default), INDIVIDUAL, GROUP or SPECIAL.",
                p -> {
                    attractions.create(p[0], p[1], p[2], optional(p, 3));
                    return "OK ATTRACTION CREATED";
                });
        registry.register("ATTRACTION DELETE", "IdAtraccion", "Deletes an attraction with nobody inside.",
                p -> {
                    attractions.delete(p[0]);
                    return "OK ATTRACTION DELETED";
                });
        registry.register("ATTRACTION ENTER", "IdAtraccion;IdEntrada[;SPEED]",
                "Lets in the users of a ticket valid today if they all fit; SPEED consumes one SpeedPass per"
                        + " ticket; SPECIAL attractions consume the Special Pass.",
                p -> describe(attractions.enter(p[0], p[1], optional(p, 2))));
        registry.register("ATTRACTION EXIT", "IdAtraccion;IdEntrada",
                "Lets out the users of a ticket that are inside.",
                p -> "OK ATTRACTION EXITED -> " + attractions.exit(p[0], p[1]));
        registry.register("ATTRACTION STATUS", "IdAtraccion",
                "Shows type, capacity, occupancy, free places and users inside.",
                p -> status(attractions.get(p[0])));
    }

    private static String optional(String[] params, int index) {
        return params.length > index ? params[index] : null;
    }

    private static String describe(EntryResult result) {
        StringBuilder text = new StringBuilder("OK ATTRACTION ENTERED -> " + result.users());
        List<Pass> consumed = result.consumed();
        if (!consumed.isEmpty()) {
            text.append(" (").append(consumed.get(0).getKind()).append(" CONSUMED: ")
                    .append(consumed.stream().map(Pass::getId).collect(Collectors.joining(", "))).append(")");
        }
        return text.toString();
    }

    private static String status(Attraction<?> a) {
        String nl = System.lineSeparator();
        StringBuilder text = new StringBuilder()
                .append("ATTRACTION ").append(a.getId()).append(" - ").append(a.getName()).append(nl)
                .append("  Type: ").append(a.getType()).append(nl)
                .append("  Capacity: ").append(a.getCapacity()).append(nl)
                .append("  Present: ").append(a.getPresent().size()).append(nl)
                .append("  Available: ").append(a.getAvailable()).append(nl)
                .append("  Users:");
        if (a.getPresent().isEmpty()) {
            text.append(nl).append("    (none)");
        }
        for (User u : a.getPresent()) {
            text.append(nl).append("    - ").append(u.getName()).append(' ').append(u.getSurname())
                    .append(" (").append(u.getDni()).append(')');
        }
        return text.toString();
    }
}
