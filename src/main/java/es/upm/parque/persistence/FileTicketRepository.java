package es.upm.parque.persistence;

import es.upm.parque.business.repository.TicketRepository;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores tickets in two text files. Individual tickets (including those inside group tickets) use
 * {@code Id;yyyy-MM-dd;price;DNI}; group tickets use {@code Id;IdGrupo;yyyy-MM-dd;total;IdTicket1,IdTicket2,...}.
 * Holders and groups that no longer exist are restored as detached objects carrying only their identifier.
 */
public class FileTicketRepository implements TicketRepository {
    private static final String SEPARATOR = ";";
    private static final String LIST_SEPARATOR = ",";
    public static final String DELETED_USER_NAME = "(deleted user)";
    public static final String DELETED_GROUP_NAME = "(deleted group)";

    private final Path individualFile;
    private final Path groupFile;

    public FileTicketRepository(Path individualFile, Path groupFile) {
        this.individualFile = individualFile;
        this.groupFile = groupFile;
    }

    @Override
    public List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById) {
        Map<String, IndividualTicket> individuals = new HashMap<>();
        List<Ticket> tickets = new ArrayList<>();
        for (String[] f : readLines(individualFile, 4)) {
            IndividualTicket t = new IndividualTicket(f[0], LocalDate.parse(f[1]), holder(f[3], userByDni),
                    new BigDecimal(f[2]));
            individuals.put(t.getId(), t);
            tickets.add(t);
        }
        for (String[] f : readLines(groupFile, 5)) {
            List<IndividualTicket> parts = new ArrayList<>();
            for (String id : f[4].split(LIST_SEPARATOR)) {
                IndividualTicket part = individuals.get(id);
                if (part == null) {
                    throw new IllegalStateException("group ticket " + f[0] + " references unknown ticket " + id);
                }
                parts.add(part);
            }
            tickets.add(new GroupTicket(f[0], LocalDate.parse(f[2]), group(f[1], groupById), parts,
                    new BigDecimal(f[3])));
        }
        return tickets;
    }

    @Override
    public void save(List<Ticket> tickets) {
        List<String> individualLines = new ArrayList<>();
        List<String> groupLines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it) {
                individualLines.add(String.join(SEPARATOR, it.getId(), it.getDate().toString(),
                        it.getPrice().toPlainString(), it.getUser().getDni()));
            } else if (t instanceof GroupTicket gt) {
                List<String> ids = new ArrayList<>();
                for (IndividualTicket part : gt.getTickets()) {
                    ids.add(part.getId());
                }
                groupLines.add(String.join(SEPARATOR, gt.getId(), gt.getGroup().getId(), gt.getDate().toString(),
                        gt.getPrice().toPlainString(), String.join(LIST_SEPARATOR, ids)));
            }
        }
        writeLines(individualFile, individualLines);
        writeLines(groupFile, groupLines);
    }

    private static List<String[]> readLines(Path file, int fields) {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(file)) {
            return rows;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(SEPARATOR, -1);
                if (f.length != fields || !isWellFormed(f)) {
                    System.err.println("Ignoring malformed ticket line: " + line);
                    continue;
                }
                rows.add(f);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read tickets file " + file + ": " + e.getMessage(), e);
        }
        return rows;
    }

    private static boolean isWellFormed(String[] f) {
        try {
            // individual: Id;date;price;DNI - group: Id;IdGrupo;date;total;ids
            int dateIndex = f.length == 4 ? 1 : 2;
            int priceIndex = f.length == 4 ? 2 : 3;
            LocalDate.parse(f[dateIndex]);
            new BigDecimal(f[priceIndex]);
            return true;
        } catch (DateTimeParseException | NumberFormatException e) {
            return false;
        }
    }

    private static void writeLines(Path file, List<String> lines) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write tickets file " + file + ": " + e.getMessage(), e);
        }
    }

    private static User holder(String dni, Function<String, User> userByDni) {
        User user = userByDni.apply(dni);
        return user != null ? user : new User(DELETED_USER_NAME, "", dni, "", "", "", "");
    }

    private static Group group(String id, Function<String, Group> groupById) {
        Group group = groupById.apply(id);
        return group != null ? group : new Group(id, DELETED_GROUP_NAME);
    }
}
