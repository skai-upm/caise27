package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.amusementpark.business.repository.TicketRepository;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;
import es.upm.etsisi.amusementpark.business.user.User;

/**
 * Stores tickets as text records, one per line:
 * <pre>
 * I;id;date;basePrice;discount;dni;firstName;surnames;email          standalone individual ticket
 * G;id;date;groupId                                                   group ticket (groupId empty once deleted)
 * C;groupTicketId;id;date;basePrice;discount;dni;firstName;surnames;email   composition ticket
 * </pre>
 * The user snapshot lets historical tickets of deleted users be rebuilt.
 */
public class FileTicketRepository implements TicketRepository {
    private static final String SEPARATOR = ";";
    private static final String INDIVIDUAL = "I";
    private static final String GROUP = "G";
    private static final String COMPOSITION = "C";

    private final Path file;
    private final List<String> warnings = new ArrayList<>();

    public FileTicketRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) throws IOException {
        warnings.clear();
        List<Ticket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        Map<String, User> people = new HashMap<>(usersByDni);
        List<Object> entries = new ArrayList<>();
        Map<String, PendingGroupTicket> pending = new HashMap<>();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            try {
                readRecord(lines.get(i).split(SEPARATOR, -1), people, entries, pending);
            } catch (IllegalArgumentException | DateTimeParseException e) {
                warnings.add("skipped malformed ticket line " + (i + 1) + " in " + file);
            }
        }
        for (Object entry : entries) {
            tickets.add(entry instanceof PendingGroupTicket p ? p.build(groupsById) : (Ticket) entry);
        }
        return tickets;
    }

    private static void readRecord(String[] f, Map<String, User> people, List<Object> entries,
                                   Map<String, PendingGroupTicket> pending) {
        switch (f[0]) {
            case INDIVIDUAL -> {
                expect(f, 9);
                entries.add(individual(f, 1, people));
            }
            case GROUP -> {
                expect(f, 4);
                PendingGroupTicket p = new PendingGroupTicket(f[1], LocalDate.parse(f[2]), f[3], new ArrayList<>());
                pending.put(f[1], p);
                entries.add(p);
            }
            case COMPOSITION -> {
                expect(f, 10);
                PendingGroupTicket p = pending.get(f[1]);
                if (p == null) {
                    throw new IllegalArgumentException("composition before its group ticket");
                }
                p.parts().add(individual(f, 2, people));
            }
            default -> throw new IllegalArgumentException("unknown record type");
        }
    }

    private record PendingGroupTicket(String id, LocalDate date, String groupId, List<IndividualTicket> parts) {
        GroupTicket build(Map<String, Group> groupsById) {
            return new GroupTicket(id, date, groupId.isEmpty() ? null : groupsById.get(groupId), parts);
        }
    }

    private static void expect(String[] fields, int count) {
        if (fields.length != count) {
            throw new IllegalArgumentException("expected " + count + " fields");
        }
    }

    private static IndividualTicket individual(String[] f, int offset, Map<String, User> people) {
        String dni = f[offset + 4];
        User user = people.computeIfAbsent(dni,
                d -> new User(f[offset + 5], f[offset + 6], d, f[offset + 7], "", "", ""));
        return new IndividualTicket(f[offset], LocalDate.parse(f[offset + 1]), user,
                new BigDecimal(f[offset + 2]), Integer.parseInt(f[offset + 3]));
    }

    @Override
    public void save(List<Ticket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket g) {
                String groupId = g.getGroup() == null ? "" : g.getGroup().getIdentifier();
                lines.add(String.join(SEPARATOR, GROUP, g.getIdentifier(), g.getDate().toString(), groupId));
                for (IndividualTicket part : g.getComposition()) {
                    lines.add(COMPOSITION + SEPARATOR + g.getIdentifier() + SEPARATOR + individualFields(part));
                }
            } else if (t instanceof IndividualTicket it) {
                lines.add(INDIVIDUAL + SEPARATOR + individualFields(it));
            }
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private static String individualFields(IndividualTicket t) {
        User u = t.getUser();
        return String.join(SEPARATOR, t.getIdentifier(), t.getDate().toString(), t.getBasePrice().toPlainString(),
                String.valueOf(t.getDiscountPercent()), u.getDni(), u.getFirstName(), u.getSurnames(), u.getEmail());
    }

    @Override
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
