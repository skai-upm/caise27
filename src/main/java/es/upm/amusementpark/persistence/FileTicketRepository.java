package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Stores tickets in a text file, one record per line.
 * <ul>
 *   <li>Standalone individual ticket:
 *       {@code INDIVIDUAL;id;date;basePrice;discount;dni;email;firstName;surnames}</li>
 *   <li>Group ticket: {@code GROUP;id;date;groupId;count}, where {@code groupId} is
 *       empty once the group was deleted, followed by {@code count} lines
 *       {@code MEMBER;...} with the same fields as an individual ticket.</li>
 * </ul>
 * The holder's identity is kept so that historical tickets survive the
 * deletion of their user.
 */
public class FileTicketRepository implements TicketRepository {

    public static final String DEFAULT_FILE_NAME = "tickets.txt";

    static final String INDIVIDUAL = "INDIVIDUAL";
    static final String GROUP = "GROUP";
    static final String MEMBER = "MEMBER";

    private static final String SEPARATOR = ";";
    private static final int INDIVIDUAL_FIELDS = 8;
    private static final int GROUP_FIELDS = 4;

    private final Path file;
    private final FileDataStore store;
    private final PrintStream warnings;

    public FileTicketRepository(Path file, FileDataStore store, PrintStream warnings) {
        this.file = file;
        this.store = store;
        this.warnings = warnings;
    }


    /**
     * Loads all tickets. Holders are resolved by DNI among the active users;
     * holders who are no longer users are rebuilt from the stored identity.
     * Groups are resolved by identifier.
     */
    @Override
    public List<Ticket> loadAll(Function<String, User> userByDni, Function<String, Group> groupById)
            throws IOException {
        Loader loader = new Loader(userByDni, groupById);
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (!line.trim().isEmpty()) {
                loader.read(line.split(SEPARATOR, -1), lineNumber);
            }
        }
        loader.closeGroup();
        return loader.tickets;
    }

    @Override
    public void saveAll(Collection<Ticket> tickets) throws IOException {
        store.write(file, toLines(tickets));
    }

    static List<String> toLines(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                Group group = groupTicket.getGroup();
                lines.add(String.join(SEPARATOR, GROUP, groupTicket.getId(), groupTicket.getDate().toString(),
                        group == null ? "" : group.getId(), String.valueOf(groupTicket.getComposition().size())));
                for (IndividualTicket member : groupTicket.getComposition()) {
                    lines.add(MEMBER + SEPARATOR + formatIndividual(member));
                }
            } else if (ticket instanceof IndividualTicket individual) {
                lines.add(INDIVIDUAL + SEPARATOR + formatIndividual(individual));
            }
        }
        return lines;
    }

    private static String formatIndividual(IndividualTicket ticket) {
        User user = ticket.getUser();
        return String.join(SEPARATOR, ticket.getId(), ticket.getDate().toString(),
                ticket.getBasePrice().toPlainString(), String.valueOf(ticket.getDiscountPercent()),
                user.getDni(), user.getEmail(), user.getFirstName(), user.getSurnames());
    }

    /** Header of a group ticket whose member lines are still being read. */
    private static final class PendingGroup {
        private final int lineNumber;
        private final String id;
        private final LocalDate date;
        private final Group group;
        private final int expected;
        private final List<IndividualTicket> members = new ArrayList<>();
        private boolean broken;

        PendingGroup(int lineNumber, String id, LocalDate date, Group group, int expected, boolean broken) {
            this.lineNumber = lineNumber;
            this.id = id;
            this.date = date;
            this.group = group;
            this.expected = expected;
            this.broken = broken;
        }
    }

    /** Parsing state for one load: known ids, rebuilt former users and the open group ticket. */
    private final class Loader {

        private final Function<String, User> userByDni;
        private final Function<String, Group> groupById;
        private final Map<String, User> formerUsers = new HashMap<>();
        private final Set<String> ids = new HashSet<>();
        private final List<Ticket> tickets = new ArrayList<>();
        private PendingGroup pending;

        Loader(Function<String, User> userByDni, Function<String, Group> groupById) {
            this.userByDni = userByDni;
            this.groupById = groupById;
        }

        void read(String[] fields, int lineNumber) {
            if (MEMBER.equals(fields[0])) {
                addMember(fields, lineNumber);
                return;
            }
            closeGroup();
            if (INDIVIDUAL.equals(fields[0])) {
                IndividualTicket ticket = parseIndividual(fields);
                if (ticket != null && ids.add(ticket.getId())) {
                    tickets.add(ticket);
                } else {
                    warn(lineNumber, "malformed or duplicate ticket");
                }
            } else if (GROUP.equals(fields[0])) {
                pending = parseGroupHeader(fields, lineNumber);
            } else {
                warn(lineNumber, "unknown ticket kind " + fields[0]);
            }
        }

        private void addMember(String[] fields, int lineNumber) {
            if (pending == null) {
                warn(lineNumber, "member line without group ticket");
                return;
            }
            IndividualTicket member = parseIndividual(fields);
            if (member == null) {
                pending.broken = true;
            } else {
                pending.members.add(member);
            }
        }

        /** Adds the open group ticket if it is complete and consistent. */
        void closeGroup() {
            if (pending == null) {
                return;
            }
            PendingGroup candidate = pending;
            pending = null;
            Set<String> newIds = new HashSet<>();
            newIds.add(candidate.id);
            for (IndividualTicket member : candidate.members) {
                newIds.add(member.getId());
            }
            boolean consistent = !candidate.broken && candidate.expected > 0
                    && candidate.members.size() == candidate.expected
                    && newIds.size() == candidate.expected + 1
                    && newIds.stream().noneMatch(ids::contains);
            if (!consistent) {
                warn(candidate.lineNumber, "malformed, incomplete or duplicate group ticket");
                return;
            }
            ids.addAll(newIds);
            tickets.add(new GroupTicket(candidate.id, candidate.date, candidate.group, candidate.members));
        }

        private PendingGroup parseGroupHeader(String[] fields, int lineNumber) {
            if (fields.length - 1 != GROUP_FIELDS || fields[1].trim().isEmpty()) {
                return new PendingGroup(lineNumber, "", null, null, 0, true);
            }
            try {
                String groupId = fields[3].trim();
                Group group = groupId.isEmpty() ? null : groupById.apply(groupId);
                return new PendingGroup(lineNumber, fields[1].trim(), LocalDate.parse(fields[2].trim()), group,
                        Integer.parseInt(fields[4].trim()), false);
            } catch (DateTimeParseException | NumberFormatException e) {
                return new PendingGroup(lineNumber, "", null, null, 0, true);
            }
        }

        /** Parses the individual-ticket fields that follow the record kind, or returns null. */
        private IndividualTicket parseIndividual(String[] fields) {
            if (fields.length - 1 != INDIVIDUAL_FIELDS) {
                return null;
            }
            String id = fields[1].trim();
            String dni = fields[5].trim();
            if (id.isEmpty() || dni.isEmpty()) {
                return null;
            }
            try {
                LocalDate date = LocalDate.parse(fields[2].trim());
                BigDecimal basePrice = new BigDecimal(fields[3].trim());
                int discount = Integer.parseInt(fields[4].trim());
                if (basePrice.signum() <= 0 || discount < 0 || discount > 100) {
                    return null;
                }
                User holder = resolve(dni, fields[6].trim(), fields[7].trim(), fields[8].trim());
                return new IndividualTicket(id, date, holder, basePrice, discount);
            } catch (DateTimeParseException | NumberFormatException e) {
                return null;
            }
        }

        private void warn(int lineNumber, String reason) {
            warnings.println("WARNING: skipping line " + lineNumber + " of " + file + ": " + reason);
        }

        private User resolve(String dni, String email, String firstName, String surnames) {
            User active = userByDni.apply(dni);
            if (active != null) {
                return active;
            }
            return formerUsers.computeIfAbsent(dni.toUpperCase(Locale.ROOT),
                    key -> new User(firstName, surnames, key, email, "", "", ""));
        }
    }
}
