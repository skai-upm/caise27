package es.upm.parque;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores tickets in two text files.
 * Group tickets: id;date;price;groupId;groupName
 * Individual tickets: id;date;price;groupTicketId;dni;name;surname;email (groupTicketId empty if standalone)
 * The user/group snapshot keeps past tickets restorable after their user or group has been deleted.
 */
public class TicketRepository {

    private final Path ticketsFile;
    private final Path groupTicketsFile;

    public TicketRepository(Path ticketsFile, Path groupTicketsFile) {
        this.ticketsFile = ticketsFile;
        this.groupTicketsFile = groupTicketsFile;
    }

    /**
     * Loads standalone individual tickets and group tickets (with their internal tickets).
     */
    public List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById) throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        Map<String, GroupTicket> groupTickets = new HashMap<>();
        if (Files.exists(groupTicketsFile)) {
            for (String line : Files.readAllLines(groupTicketsFile, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(";", -1);
                if (f.length != 5) {
                    System.err.println("WARNING skipping malformed group ticket line: " + line);
                    continue;
                }
                Group group = groupById.apply(f[3]);
                if (group == null) {
                    group = new Group(f[3], f[4]);
                }
                GroupTicket gt = new GroupTicket(f[0], LocalDate.parse(f[1]), group, new BigDecimal(f[2]));
                groupTickets.put(gt.getId(), gt);
                tickets.add(gt);
            }
        }
        if (Files.exists(ticketsFile)) {
            for (String line : Files.readAllLines(ticketsFile, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(";", -1);
                if (f.length != 8) {
                    System.err.println("WARNING skipping malformed ticket line: " + line);
                    continue;
                }
                User user = userByDni.apply(f[4]);
                if (user == null) {
                    user = new User(f[5], f[6], f[4], f[7], "", "", "");
                }
                IndividualTicket t = new IndividualTicket(f[0], LocalDate.parse(f[1]), user, new BigDecimal(f[2]));
                if (f[3].isEmpty()) {
                    tickets.add(t);
                } else {
                    GroupTicket owner = groupTickets.get(f[3]);
                    if (owner == null) {
                        System.err.println("WARNING skipping ticket of unknown group ticket: " + line);
                        continue;
                    }
                    owner.addTicket(t);
                }
            }
        }
        return tickets;
    }

    public void save(List<Ticket> tickets) throws IOException {
        Files.createDirectories(ticketsFile.toAbsolutePath().getParent());
        Files.createDirectories(groupTicketsFile.toAbsolutePath().getParent());
        try (BufferedWriter individual = Files.newBufferedWriter(ticketsFile, StandardCharsets.UTF_8);
             BufferedWriter group = Files.newBufferedWriter(groupTicketsFile, StandardCharsets.UTF_8)) {
            for (Ticket t : tickets) {
                if (t instanceof GroupTicket gt) {
                    group.write(String.join(";", gt.getId(), gt.getDate().toString(), gt.getPrice().toPlainString(),
                            gt.getGroup().getId(), gt.getGroup().getName()));
                    group.newLine();
                    for (IndividualTicket it : gt.getTickets()) {
                        writeIndividual(individual, it, gt.getId());
                    }
                } else if (t instanceof IndividualTicket it) {
                    writeIndividual(individual, it, "");
                }
            }
        }
    }

    private static void writeIndividual(BufferedWriter writer, IndividualTicket t, String groupTicketId)
            throws IOException {
        User u = t.getUser();
        writer.write(String.join(";", t.getId(), t.getDate().toString(), t.getPrice().toPlainString(), groupTicketId,
                u.getDni(), u.getName(), u.getSurname(), u.getEmail()));
        writer.newLine();
    }
}
