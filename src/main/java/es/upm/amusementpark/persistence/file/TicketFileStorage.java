package es.upm.amusementpark.persistence.file;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

/**
 * Stores tickets of every type. Each record keeps the user data needed to rebuild historical
 * tickets of users that no longer exist:
 *
 * <pre>
 * I;id;date;basePrice;dni;firstName;surnames;email
 * G;id;date;basePrice;groupId          (groupId empty once the group was deleted)
 * M;groupTicketId;dni;firstName;surnames;email   (one per member of the preceding G record)
 * </pre>
 */
public class TicketFileStorage {

    private final Path file;

    public TicketFileStorage(Path file) {
        this.file = file;
    }

    /**
     * Loads tickets, resolving users by DNI and groups by identifier. Users that no longer exist
     * are rebuilt from the stored data.
     */
    public List<Ticket> load(Function<String, Optional<User>> userByDni, Function<String, Optional<Group>> groupById,
            List<String> warnings) throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        PendingGroupTicket pending = null;
        int n = 0;
        for (String line : TextFiles.readLines(file)) {
            n++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            try {
                switch (f[0]) {
                    case "I" -> {
                        pending = flush(pending, tickets);
                        User user = user(userByDni, f[4], f[5], f[6], f[7]);
                        tickets.add(new IndividualTicket(f[1], LocalDate.parse(f[2]), user, new BigDecimal(f[3])));
                    }
                    case "G" -> {
                        pending = flush(pending, tickets);
                        Group group = f[4].isEmpty() ? null : groupById.apply(f[4]).orElse(null);
                        pending = new PendingGroupTicket(f[1], LocalDate.parse(f[2]), new BigDecimal(f[3]), group);
                    }
                    case "M" -> {
                        if (pending == null || !pending.id.equals(f[1])) {
                            throw new IllegalArgumentException("member without group ticket");
                        }
                        pending.members.add(user(userByDni, f[2], f[3], f[4], f[5]));
                    }
                    default -> throw new IllegalArgumentException("unknown record");
                }
            } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException | DateTimeParseException e) {
                warnings.add("Skipping malformed ticket record at line " + n + " of " + file);
            }
        }
        flush(pending, tickets);
        return tickets;
    }

    public void save(List<Ticket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket g) {
                lines.add(join("G", g.getId(), g.getDate().toString(), basePrice(g).toPlainString(),
                        g.getGroup() == null ? "" : g.getGroup().getId()));
                for (IndividualTicket member : g.individualTickets()) {
                    User u = member.getUser();
                    lines.add(join("M", g.getId(), u.getDni(), u.getFirstName(), u.getSurnames(), u.getEmail()));
                }
            } else if (t instanceof IndividualTicket i) {
                User u = i.getUser();
                lines.add(join("I", i.getId(), i.getDate().toString(), i.getBasePrice().toPlainString(), u.getDni(),
                        u.getFirstName(), u.getSurnames(), u.getEmail()));
            }
        }
        TextFiles.writeLines(file, lines);
    }

    private static BigDecimal basePrice(GroupTicket ticket) {
        return ticket.individualTickets().get(0).getBasePrice();
    }

    private static String join(String... values) {
        return String.join(TextFiles.SEPARATOR, values);
    }

    private static User user(Function<String, Optional<User>> userByDni, String dni, String firstName,
            String surnames, String email) {
        return userByDni.apply(dni).orElseGet(() -> new User(firstName, surnames, dni, email, "", "", ""));
    }

    private static PendingGroupTicket flush(PendingGroupTicket pending, List<Ticket> tickets) {
        if (pending != null && !pending.members.isEmpty()) {
            tickets.add(new GroupTicket(pending.id, pending.date, pending.group, pending.members, pending.basePrice));
        }
        return null;
    }

    private static final class PendingGroupTicket {
        private final String id;
        private final LocalDate date;
        private final BigDecimal basePrice;
        private final Group group;
        private final List<User> members = new ArrayList<>();

        PendingGroupTicket(String id, LocalDate date, BigDecimal basePrice, Group group) {
            this.id = id;
            this.date = date;
            this.basePrice = basePrice;
            this.group = group;
        }
    }
}
