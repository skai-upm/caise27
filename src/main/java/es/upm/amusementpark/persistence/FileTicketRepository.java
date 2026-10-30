package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Formats;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores individual tickets ({@code id;date;price;dni;email;name;surname}) and group tickets
 * ({@code G;id;groupId;groupName;date;price} followed by one {@code T;...} line per constituent ticket).
 * User and group data are stored as snapshots so historical tickets survive deletion of their user or group.
 */
public class FileTicketRepository implements TicketRepository {

    private final Path ticketsFile;
    private final Path groupTicketsFile;

    public FileTicketRepository(Path ticketsFile, Path groupTicketsFile) {
        this.ticketsFile = ticketsFile;
        this.groupTicketsFile = groupTicketsFile;
    }

    public List<IndividualTicket> loadTickets(List<User> users) throws IOException {
        List<IndividualTicket> tickets = new ArrayList<>();
        for (String line : readLines(ticketsFile)) {
            String[] f = line.split(";", -1);
            IndividualTicket ticket = f.length == 7 ? parseTicket(f, 0, users) : null;
            if (ticket == null) {
                System.err.println("WARNING: skipping malformed ticket record: " + line);
            } else {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    public List<GroupTicket> loadGroupTickets(List<User> users, List<Group> groups) throws IOException {
        List<GroupTicket> result = new ArrayList<>();
        String[] header = null;
        List<IndividualTicket> constituents = new ArrayList<>();
        for (String line : readLines(groupTicketsFile)) {
            String[] f = line.split(";", -1);
            if (f[0].equals("G") && f.length == 6) {
                addGroupTicket(result, header, constituents, groups);
                header = f;
                constituents = new ArrayList<>();
            } else if (f[0].equals("T") && f.length == 8 && header != null) {
                IndividualTicket t = parseTicket(f, 1, users);
                if (t != null) {
                    constituents.add(t);
                }
            } else {
                System.err.println("WARNING: skipping malformed group ticket record: " + line);
            }
        }
        addGroupTicket(result, header, constituents, groups);
        return result;
    }

    public void save(List<IndividualTicket> tickets, List<GroupTicket> groupTickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            lines.add(format(t));
        }
        TextFiles.write(ticketsFile, lines);
        List<String> groupLines = new ArrayList<>();
        for (GroupTicket gt : groupTickets) {
            groupLines.add(String.join(";", "G", gt.getId(), gt.getGroup().getId(), gt.getGroup().getName(),
                    gt.getDate().toString(), Formats.formatPrice(gt.getPrice())));
            for (IndividualTicket t : gt.getTickets()) {
                groupLines.add("T;" + format(t));
            }
        }
        TextFiles.write(groupTicketsFile, groupLines);
    }

    private static void addGroupTicket(List<GroupTicket> result, String[] header, List<IndividualTicket> constituents,
            List<Group> groups) {
        if (header == null) {
            return;
        }
        LocalDate date = Formats.parseDate(header[4]);
        if (date == null) {
            System.err.println("WARNING: skipping group ticket with invalid date: " + header[1]);
            return;
        }
        Group group = null;
        for (Group g : groups) {
            if (g.getId().equals(header[2])) {
                group = g;
            }
        }
        if (group == null) {
            group = new Group(header[2], header[3]);
        }
        result.add(new GroupTicket(header[1], date, group, constituents, new BigDecimal(header[5])));
    }

    private static String format(IndividualTicket t) {
        User u = t.getUser();
        return String.join(";", t.getId(), t.getDate().toString(), Formats.formatPrice(t.getPrice()), u.getDni(),
                u.getEmail(), u.getName(), u.getSurname());
    }

    /** Parses {@code id;date;price;dni;email;name;surname} starting at {@code offset}. */
    private static IndividualTicket parseTicket(String[] f, int offset, List<User> users) {
        LocalDate date = Formats.parseDate(f[offset + 1]);
        if (date == null) {
            return null;
        }
        String dni = f[offset + 3];
        User user = FileGroupRepository.findByDni(users, dni);
        if (user == null) {
            // Historical ticket of a deleted user: rebuild an unregistered user from the snapshot.
            user = new User(f[offset + 5], f[offset + 6], dni, f[offset + 4], "", "", "");
        }
        return new IndividualTicket(f[offset], date, user, new BigDecimal(f[offset + 2]));
    }

    private static List<String> readLines(Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        if (Files.exists(file)) {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }
}
