package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores tickets in two text files. Individual tickets: {@code id;dni;yyyy-MM-dd;price;groupTicketId}
 * (the last field is empty for standalone tickets and may be absent in older files). Group tickets:
 * {@code id;groupId;yyyy-MM-dd;total}; their composition is given by the individual tickets that
 * name them.
 */
public class TicketRepository {

    /** Tickets read from storage. {@code individual} includes the internal tickets of group tickets. */
    public record TicketData(List<IndividualTicket> individual, List<GroupTicket> group) {
    }

    private final Path individualFile;
    private final Path groupFile;

    public TicketRepository(Path individualFile, Path groupFile) {
        this.individualFile = individualFile;
        this.groupFile = groupFile;
    }

    /**
     * Loads every ticket, linking it to the already-loaded users (by DNI) and groups (by id).
     * Lines that cannot be resolved or parsed are skipped.
     */
    public TicketData load(Map<String, User> usersByDni, Map<String, Group> groupsById) {
        Map<String, GroupTicket> groupTickets = new HashMap<>();
        List<GroupTicket> group = new ArrayList<>();
        for (String line : readLines(groupFile)) {
            GroupTicket ticket = parseGroupTicket(line, groupsById);
            if (ticket != null) {
                group.add(ticket);
                groupTickets.put(ticket.getId(), ticket);
            }
        }
        List<IndividualTicket> individual = new ArrayList<>();
        for (String line : readLines(individualFile)) {
            String[] f = line.split(";", -1);
            IndividualTicket ticket = parseIndividual(f, usersByDni);
            if (ticket != null && attachToOwner(ticket, f, groupTickets)) {
                individual.add(ticket);
            }
        }
        return new TicketData(individual, group);
    }

    /**
     * Adds an internal ticket to the group ticket named in its line. Returns false if that group
     * ticket is unknown; standalone tickets are accepted unchanged.
     */
    private static boolean attachToOwner(IndividualTicket ticket, String[] f, Map<String, GroupTicket> groupTickets) {
        if (f.length < 5 || f[4].isEmpty()) {
            return true;
        }
        GroupTicket owner = groupTickets.get(f[4]);
        if (owner == null) {
            return false;
        }
        owner.addTicket(ticket);
        return true;
    }

    public void save(List<IndividualTicket> individual, List<GroupTicket> group) {
        Map<IndividualTicket, String> owners = new HashMap<>();
        List<String> groupLines = new ArrayList<>();
        for (GroupTicket g : group) {
            g.getTickets().forEach(t -> owners.put(t, g.getId()));
            groupLines.add(String.join(";", g.getId(), g.getGroup().getId(),
                    MainController.formatDate(g.getDate()), MainController.formatPrice(g.getPrice())));
        }
        List<String> individualLines = individual.stream()
                .map(t -> String.join(";", t.getId(), t.getUser().getDni(), MainController.formatDate(t.getDate()),
                        MainController.formatPrice(t.getPrice()), owners.getOrDefault(t, "")))
                .toList();
        write(groupFile, groupLines);
        write(individualFile, individualLines);
    }

    private static IndividualTicket parseIndividual(String[] f, Map<String, User> usersByDni) {
        if ((f.length != 4 && f.length != 5) || !usersByDni.containsKey(f[1])) {
            return null;
        }
        try {
            return new IndividualTicket(f[0], MainController.parseDate(f[2]), usersByDni.get(f[1]),
                    new BigDecimal(f[3]));
        } catch (ParkException | NumberFormatException e) {
            return null;
        }
    }

    private static GroupTicket parseGroupTicket(String line, Map<String, Group> groupsById) {
        String[] f = line.split(";", -1);
        if (f.length != 4 || !groupsById.containsKey(f[1])) {
            return null;
        }
        try {
            return new GroupTicket(f[0], MainController.parseDate(f[2]), groupsById.get(f[1]),
                    new BigDecimal(f[3]), List.of());
        } catch (ParkException | NumberFormatException e) {
            return null;
        }
    }

    private static List<String> readLines(Path file) {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read tickets file " + file, e);
        }
    }

    private static void write(Path file, List<String> lines) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write tickets file " + file, e);
        }
    }
}
