package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes all tickets in one file. Individual tickets are stored as
 * {@code INDIVIDUAL;id;date;price;groupTicketId;dni;firstName;surnames;email}
 * and group tickets as {@code GROUP;id;date;groupId}. The user data kept with
 * each individual ticket allows historical tickets to be rebuilt after their
 * user has been deleted.
 */
public class TicketFileStorage {

    private static final String INDIVIDUAL = "INDIVIDUAL";
    private static final String GROUP = "GROUP";

    private final Path file;
    private int skippedLines;

    public TicketFileStorage(Path file) {
        this.file = file;
    }

    public List<Ticket> load(UserRepository users, GroupRepository groups) {
        skippedLines = 0;
        List<String[]> records = TextFiles.readRecords(file);
        Map<String, User> formerUsers = new HashMap<>();
        Map<String, List<IndividualTicket>> compositions = new LinkedHashMap<>();
        List<Ticket> tickets = new ArrayList<>();
        for (String[] f : records) {
            if (f.length != 9 || !f[0].equals(INDIVIDUAL)) {
                continue;
            }
            try {
                User user = users.findByDni(f[5]).orElseGet(() -> formerUsers.computeIfAbsent(f[5],
                        dni -> new User(f[6], f[7], dni, f[8], "", "", "")));
                IndividualTicket ticket = new IndividualTicket(f[1], LocalDate.parse(f[2]), user, new BigDecimal(f[3]));
                if (f[4].isEmpty()) {
                    tickets.add(ticket);
                } else {
                    compositions.computeIfAbsent(f[4], id -> new ArrayList<>()).add(ticket);
                }
            } catch (DateTimeParseException | NumberFormatException e) {
                skippedLines++;
            }
        }
        for (String[] f : records) {
            if (f.length == 9 && f[0].equals(INDIVIDUAL)) {
                continue;
            }
            if (f.length != 4 || !f[0].equals(GROUP)) {
                skippedLines++;
                continue;
            }
            try {
                List<IndividualTicket> composition = compositions.getOrDefault(f[1], List.of());
                Group group = f[3].isEmpty() ? null : groups.findById(f[3]).orElse(null);
                GroupTicket groupTicket = new GroupTicket(f[1], LocalDate.parse(f[2]), group, composition);
                tickets.addAll(composition);
                tickets.add(groupTicket);
            } catch (DateTimeParseException e) {
                skippedLines++;
            }
        }
        return tickets;
    }

    public void save(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof IndividualTicket t) {
                User u = t.getUser();
                String owner = t.isPartOfGroupTicket() ? t.getGroupTicket().getIdentifier() : "";
                lines.add(String.join(";", INDIVIDUAL, t.getIdentifier(), t.getDate().toString(),
                        t.getPrice().toPlainString(), owner, u.getDni(), u.getFirstName(), u.getSurnames(),
                        u.getEmail()));
            } else if (ticket instanceof GroupTicket g) {
                String group = g.getGroup() == null ? "" : g.getGroup().getIdentifier();
                lines.add(String.join(";", GROUP, g.getIdentifier(), g.getDate().toString(), group));
            }
        }
        TextFiles.writeLines(file, lines);
    }

    public int getSkippedLines() {
        return skippedLines;
    }
}
