package es.upm.parque.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
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

import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Reads and writes groups, tickets and attractions as ';'-separated text files in a data directory.
 *
 * <ul>
 *   <li>{@code groups.txt}: {@code id;name;dni1,dni2,...}</li>
 *   <li>{@code tickets.txt}: {@code I;id;date;price;dni;email;firstName;surnames} and
 *       {@code G;id;date;groupIdOrEmpty;componentId1,componentId2,...}</li>
 *   <li>{@code attractions.txt}: {@code id;name;capacity;type} (occupants are never stored; a line without a type
 *       is a GENERAL attraction)</li>
 * </ul>
 */
public class ParkFileStore {

    public static final String GROUPS_FILE = "groups.txt";
    public static final String TICKETS_FILE = "tickets.txt";
    public static final String ATTRACTIONS_FILE = "attractions.txt";

    private static final String SEP = ";";
    private static final String LIST_SEP = ",";
    private static final String INDIVIDUAL = "I";
    private static final String GROUP = "G";

    private final Path dataDir;
    private int malformedLineCount;

    public ParkFileStore(Path dataDir) {
        this.dataDir = dataDir;
    }

    /** Number of lines skipped by the last {@link #load} because they could not be reconstructed. */
    public int getMalformedLineCount() {
        return malformedLineCount;
    }

    /** Loads groups, tickets and attractions, linking them to the given users. */
    public void load(UserRepository users, GroupRepository groups, TicketRepository tickets,
                     AttractionRepository attractions) {
        malformedLineCount = 0;
        loadGroups(users, groups);
        loadTickets(users, groups, tickets);
        loadAttractions(attractions);
    }

    /** Replaces the three files with the current groups, tickets and attractions. */
    public void save(GroupRepository groups, TicketRepository tickets, AttractionRepository attractions) {
        List<String> groupLines = new ArrayList<>();
        for (Group g : groups.findAll()) {
            List<String> dnis = g.getMembers().stream().map(User::getDni).toList();
            groupLines.add(String.join(SEP, g.getId(), g.getName(), String.join(LIST_SEP, dnis)));
        }
        List<String> ticketLines = new ArrayList<>();
        for (Ticket t : tickets.findAll()) {
            if (t instanceof IndividualTicket i) {
                User u = i.getUser();
                ticketLines.add(String.join(SEP, INDIVIDUAL, i.getId(), i.getDate().toString(),
                        i.getPrice().toPlainString(), u.getDni(), u.getEmail(), u.getFirstName(), u.getSurnames()));
            }
        }
        for (Ticket t : tickets.findAll()) {
            if (t instanceof GroupTicket g) {
                List<String> ids = g.getComposition().stream().map(Ticket::getId).toList();
                String groupId = g.getGroup() == null ? "" : g.getGroup().getId();
                ticketLines.add(String.join(SEP, GROUP, g.getId(), g.getDate().toString(), groupId,
                        String.join(LIST_SEP, ids)));
            }
        }
        List<String> attractionLines = attractions.findAll().stream()
                .map(a -> String.join(SEP, a.getId(), a.getName(), Integer.toString(a.getCapacity()),
                        a.getType().name()))
                .toList();
        write(GROUPS_FILE, groupLines);
        write(TICKETS_FILE, ticketLines);
        write(ATTRACTIONS_FILE, attractionLines);
    }

    private void loadGroups(UserRepository users, GroupRepository groups) {
        for (String[] f : rows(GROUPS_FILE)) {
            if (f.length != 3 || f[0].isEmpty() || groups.exists(f[0])) {
                malformedLineCount++;
                continue;
            }
            Group group = new Group(f[0], f[1]);
            for (String dni : f[2].isEmpty() ? new String[0] : f[2].split(LIST_SEP)) {
                User member = users.findByDni(dni);
                if (member != null) {
                    group.add(member);
                }
            }
            groups.add(group);
        }
    }

    private void loadTickets(UserRepository users, GroupRepository groups, TicketRepository tickets) {
        List<String[]> groupRows = new ArrayList<>();
        Map<String, IndividualTicket> individuals = new HashMap<>();
        for (String[] f : rows(TICKETS_FILE)) {
            if (f.length == 8 && INDIVIDUAL.equals(f[0])) {
                IndividualTicket ticket = individual(f, users);
                if (ticket == null || tickets.exists(ticket.getId())) {
                    malformedLineCount++;
                } else {
                    tickets.add(ticket);
                    individuals.put(ticket.getId(), ticket);
                }
            } else if (f.length == 5 && GROUP.equals(f[0])) {
                groupRows.add(f);
            } else {
                malformedLineCount++;
            }
        }
        for (String[] f : groupRows) {
            GroupTicket ticket = groupTicket(f, groups, individuals);
            if (ticket == null || tickets.exists(ticket.getId())) {
                malformedLineCount++;
            } else {
                tickets.add(ticket);
            }
        }
    }

    private static IndividualTicket individual(String[] f, UserRepository users) {
        LocalDate date = date(f[2]);
        BigDecimal price = price(f[3]);
        if (f[1].isEmpty() || date == null || price == null) {
            return null;
        }
        User holder = users.findByDni(f[4]);
        if (holder == null) {
            holder = new User(f[6], f[7], f[4], f[5], "", "", "");
        }
        return new IndividualTicket(f[1], holder, date, price);
    }

    private static GroupTicket groupTicket(String[] f, GroupRepository groups, Map<String, IndividualTicket> individuals) {
        LocalDate date = date(f[2]);
        if (f[1].isEmpty() || date == null || f[4].isEmpty()) {
            return null;
        }
        List<IndividualTicket> composition = new ArrayList<>();
        for (String id : f[4].split(LIST_SEP)) {
            IndividualTicket component = individuals.get(id);
            if (component == null) {
                return null;
            }
            composition.add(component);
        }
        Group group = f[3].isEmpty() ? null : groups.findById(f[3]);
        return new GroupTicket(f[1], group, date, composition);
    }

    private void loadAttractions(AttractionRepository attractions) {
        for (String[] f : rows(ATTRACTIONS_FILE)) {
            int capacity = f.length == 3 || f.length == 4 ? capacity(f[2]) : -1;
            // Lines without a type (Delivery 2 data) are general attractions.
            AttractionType type = f.length == 4 ? AttractionType.fromName(f[3]) : AttractionType.GENERAL;
            if (capacity <= 0 || type == null || f[0].isEmpty() || attractions.exists(f[0])) {
                malformedLineCount++;
            } else {
                attractions.add(type.create(f[0], f[1], capacity));
            }
        }
    }

    private List<String[]> rows(String fileName) {
        Path file = dataDir.resolve(fileName);
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                    .filter(line -> !line.isBlank())
                    .map(line -> line.split(SEP, -1))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + file, e);
        }
    }

    private void write(String fileName, List<String> lines) {
        Path file = dataDir.resolve(fileName);
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write " + file, e);
        }
    }

    private static LocalDate date(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static BigDecimal price(String text) {
        try {
            BigDecimal value = new BigDecimal(text);
            return value.signum() > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int capacity(String text) {
        if (text.isEmpty() || text.length() > 9) {
            return -1;
        }
        int value = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
            value = value * 10 + (c - '0');
        }
        return value;
    }
}
