package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores group tickets, one per line: {@code Id;IdGrupo;yyyy-MM-dd;PrecioTotal;PrecioIndividual;DNI1,DNI2,...}.
 * Constituent tickets are rebuilt from the DNIs (in purchase order) and linked to the loaded users; groups
 * and users that no longer exist are kept as historical identifiers. Only class touching the file.
 */
public class GroupTicketFileStorage {

    public static final String DEFAULT_FILE = "grouptickets.txt";
    private static final String SEPARATOR = ";";
    private static final String MEMBER_SEPARATOR = ",";
    private static final int FIELDS = 6;

    private final Path file;

    public GroupTicketFileStorage(Path file) {
        this.file = file;
    }

    public List<GroupTicket> load(List<User> users, List<Group> groups) {
        List<GroupTicket> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read group ticket file " + file, e);
        }
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            try {
                result.add(parse(line, users, groups));
            } catch (IllegalArgumentException | DateTimeParseException | ArithmeticException e) {
                System.err.println("Skipping malformed group ticket record: " + line);
            }
        }
        return result;
    }

    private static GroupTicket parse(String line, List<User> users, List<Group> groups) {
        String[] f = line.split(SEPARATOR, -1);
        if (f.length != FIELDS || f[5].isEmpty()) {
            throw new IllegalArgumentException("wrong number of fields");
        }
        String id = f[0];
        LocalDate date = LocalDate.parse(f[2]);
        BigDecimal total = new BigDecimal(f[3]);
        BigDecimal unitPrice = new BigDecimal(f[4]);
        Group group = groups.stream().filter(g -> g.getId().equals(f[1])).findFirst().orElse(null);
        String[] dnis = f[5].split(MEMBER_SEPARATOR);
        List<Ticket> tickets = new ArrayList<>(dnis.length);
        for (int i = 0; i < dnis.length; i++) {
            String dni = dnis[i];
            String ticketId = GroupTicket.constituentId(id, i + 1);
            Optional<User> owner = users.stream().filter(u -> u.getDni().equals(dni)).findFirst();
            tickets.add(owner.map(u -> new Ticket(ticketId, u, date, unitPrice))
                    .orElseGet(() -> new Ticket(ticketId, dni, date, unitPrice)));
        }
        return new GroupTicket(id, group, f[1], date, unitPrice, tickets, total);
    }

    public void save(List<GroupTicket> groupTickets) {
        List<String> lines = new ArrayList<>(groupTickets.size());
        for (GroupTicket g : groupTickets) {
            List<String> dnis = g.getTickets().stream().map(Ticket::getOwnerDni).toList();
            lines.add(String.join(SEPARATOR, g.getId(), g.getGroupId(), g.getDate().toString(),
                    g.getPrice().toPlainString(), g.getUnitPrice().toPlainString(), String.join(MEMBER_SEPARATOR, dnis)));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write group ticket file " + file, e);
        }
    }
}
