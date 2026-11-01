package es.upm.amusementpark.persistence;

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

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.TicketRepository;

/**
 * Stores individual and group tickets with their passes in a text file. Record types:
 * <ul>
 *   <li>{@code I;id;date;price;dni;name;surname;email;specialPassIssued} - individual ticket</li>
 *   <li>{@code G;id;date;price;groupId;groupName} - group ticket, followed by</li>
 *   <li>{@code C;id;date;price;dni;name;surname;email;specialPassIssued} - one line per constituent ticket</li>
 *   <li>{@code S;id;price;consumed} / {@code X;id;price;consumed} - SpeedPass / Special Pass of the
 *       preceding I or C ticket</li>
 * </ul>
 * Holder name, surname and email are kept so that historical tickets survive the
 * deletion of their user; a deleted holder or group is rebuilt from that data.
 * Lines written by Delivery 2 (no {@code specialPassIssued} field, no passes) still load.
 */
public class FileTicketRepository implements TicketRepository {

    private static final String SEPARATOR = ";";
    private static final String INDIVIDUAL = "I";
    private static final String GROUP = "G";
    private static final String CONSTITUENT = "C";
    private static final String SPEED_PASS = "S";
    private static final String SPECIAL_PASS = "X";
    private static final int E2_TICKET_FIELDS = 8;
    private static final int TICKET_FIELDS = 9;
    private static final int GROUP_FIELDS = 6;
    private static final int PASS_FIELDS = 4;

    private final Path file;

    public FileTicketRepository(Path file) {
        this.file = file;
    }

    /** Mutable state of one load: lookups and the records still being assembled. */
    private static final class LoadState {
        final List<Ticket> tickets = new ArrayList<>();
        final Map<String, User> holders;
        final Map<String, Group> groups;
        String[] pendingGroup;
        List<IndividualTicket> constituents = new ArrayList<>();
        IndividualTicket lastIndividual;

        LoadState(Map<String, User> usersByDni, Map<String, Group> groupsById) {
            holders = new HashMap<>(usersByDni);
            groups = new HashMap<>(groupsById);
        }

        void closePendingGroup() {
            if (pendingGroup != null) {
                Group group = groups.computeIfAbsent(pendingGroup[4], id -> new Group(id, pendingGroup[5]));
                tickets.add(new GroupTicket(pendingGroup[1], group, LocalDate.parse(pendingGroup[2]),
                        new BigDecimal(pendingGroup[3]), constituents));
                pendingGroup = null;
                constituents = new ArrayList<>();
            }
        }
    }

    @Override
    public List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) throws IOException {
        LoadState state = new LoadState(usersByDni, groupsById);
        if (!Files.exists(file)) {
            return state.tickets;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            boolean valid;
            try {
                valid = read(line.split(SEPARATOR, -1), state);
            } catch (DateTimeParseException | NumberFormatException e) {
                valid = false;
            }
            if (!valid) {
                System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
            }
        }
        state.closePendingGroup();
        return state.tickets;
    }

    /** Applies one record to the load state; returns false when the record is malformed. */
    private static boolean read(String[] parts, LoadState state) {
        String type = parts[0];
        if (SPEED_PASS.equals(type) || SPECIAL_PASS.equals(type)) {
            return readPass(parts, state.lastIndividual);
        }
        boolean ticketLine = parts.length == TICKET_FIELDS || parts.length == E2_TICKET_FIELDS;
        if (CONSTITUENT.equals(type) && state.pendingGroup != null && ticketLine) {
            state.lastIndividual = individual(parts, state.holders);
            state.constituents.add(state.lastIndividual);
            return true;
        }
        state.closePendingGroup();
        state.lastIndividual = null;
        if (INDIVIDUAL.equals(type) && ticketLine) {
            state.lastIndividual = individual(parts, state.holders);
            state.tickets.add(state.lastIndividual);
            return true;
        }
        if (GROUP.equals(type) && parts.length == GROUP_FIELDS) {
            state.pendingGroup = parts;
            return true;
        }
        return false;
    }

    private static boolean readPass(String[] parts, IndividualTicket ticket) {
        if (ticket == null || parts.length != PASS_FIELDS) {
            return false;
        }
        BigDecimal price = new BigDecimal(parts[2]);
        boolean consumed = Boolean.parseBoolean(parts[3]);
        if (SPEED_PASS.equals(parts[0])) {
            ticket.addSpeedPass(new SpeedPass(parts[1], price, consumed));
        } else if (ticket.getSpecialPass() == null) {
            ticket.restoreSpecialPass(new SpecialPass(parts[1], price, consumed));
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void save(List<Ticket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                lines.add(String.join(SEPARATOR, GROUP, groupTicket.getId(), groupTicket.getDate().toString(),
                        groupTicket.getPrice().toPlainString(), groupTicket.getGroup().getId(),
                        groupTicket.getGroup().getName()));
                for (IndividualTicket constituent : groupTicket.getTickets()) {
                    addIndividual(lines, CONSTITUENT, constituent);
                }
            } else if (ticket instanceof IndividualTicket individual) {
                addIndividual(lines, INDIVIDUAL, individual);
            }
        }
        TextFiles.writeAtomically(file, lines);
    }

    private static void addIndividual(List<String> lines, String type, IndividualTicket ticket) {
        User user = ticket.getUser();
        lines.add(String.join(SEPARATOR, type, ticket.getId(), ticket.getDate().toString(),
                ticket.getPrice().toPlainString(), user.getDni(), user.getName(), user.getSurname(),
                user.getEmail(), String.valueOf(ticket.isSpecialPassIssued())));
        for (Pass pass : ticket.getPasses()) {
            String passType = pass instanceof SpecialPass ? SPECIAL_PASS : SPEED_PASS;
            lines.add(String.join(SEPARATOR, passType, pass.getId(), pass.getPrice().toPlainString(),
                    String.valueOf(pass.isConsumed())));
        }
    }

    private static IndividualTicket individual(String[] parts, Map<String, User> holders) {
        User user = holders.computeIfAbsent(parts[4],
                dni -> new User(parts[5], parts[6], dni, parts[7], "", "", ""));
        IndividualTicket ticket = new IndividualTicket(parts[1], LocalDate.parse(parts[2]),
                new BigDecimal(parts[3]), user);
        if (parts.length == TICKET_FIELDS && Boolean.parseBoolean(parts[8])) {
            ticket.markSpecialPassIssued();
        }
        return ticket;
    }
}
