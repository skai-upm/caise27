package es.upm.etsisi.amusementpark;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores tickets in a text file. Group tickets and individual tickets use one line each:
 * <pre>
 * GT;Identifier;Date;GroupIdentifier-or-empty
 * IT;Identifier;Date;BasePrice;Discount;OwnerGroupTicket-or-empty;Dni;Email;FirstName;Surnames
 * </pre>
 * Individual tickets keep their holder's identifying data so that historical
 * tickets of deleted users can be reconstructed.
 */
public class TicketRepository {

    private static final String SEPARATOR = ";";
    private static final String GROUP_TICKET = "GT";
    private static final String INDIVIDUAL_TICKET = "IT";
    private static final int GROUP_FIELDS = 4;
    private static final int INDIVIDUAL_FIELDS = 10;

    private final Path file;
    private final PrintStream warnings;

    public TicketRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    /** Raw group ticket line, completed with its composition once all lines are read. */
    private record GroupTicketRecord(String identifier, LocalDate date, Group group, List<IndividualTicket> composition) {
    }

    /** A composed ticket waiting to be attached to its owner group ticket. */
    private record PendingMember(String owner, IndividualTicket ticket, int lineNumber) {
    }

    /**
     * Loads tickets, linking holders to the given users by DNI (or to detached
     * users rebuilt from the stored data) and group tickets to the given groups.
     */
    public List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) throws ParkException {
        List<Object> topLevel = new ArrayList<>();
        Map<String, GroupTicketRecord> groupTickets = new HashMap<>();
        List<PendingMember> members = new ArrayList<>();
        Map<String, User> detachedUsers = new HashMap<>();
        Set<String> identifiers = new HashSet<>();
        int lineNumber = 0;
        for (String line : TextFile.readLines(file)) {
            lineNumber++;
            String[] fields = line.split(SEPARATOR, -1);
            Object entry = line.isBlank() ? null : parse(fields, usersByDni, groupsById, detachedUsers);
            if (entry == null || !identifiers.add(fields[1])) {
                if (!line.isBlank()) {
                    warn(lineNumber);
                }
            } else if (entry instanceof IndividualTicket member && !fields[5].isEmpty()) {
                members.add(new PendingMember(fields[5], member, lineNumber));
            } else {
                topLevel.add(entry);
                if (entry instanceof GroupTicketRecord groupTicket) {
                    groupTickets.put(groupTicket.identifier(), groupTicket);
                }
            }
        }
        for (PendingMember member : members) {
            GroupTicketRecord owner = groupTickets.get(member.owner());
            if (owner == null) {
                warn(member.lineNumber());
            } else {
                owner.composition().add(member.ticket());
            }
        }
        return build(topLevel);
    }

    private List<Ticket> build(List<Object> topLevel) {
        List<Ticket> tickets = new ArrayList<>();
        for (Object entry : topLevel) {
            if (entry instanceof GroupTicketRecord gt) {
                if (gt.composition().isEmpty()) {
                    warnings.println("WARNING: skipping group ticket " + gt.identifier() + " without composition in " + file);
                } else {
                    tickets.add(new GroupTicket(gt.identifier(), gt.date(), gt.group(), gt.composition()));
                }
            } else {
                tickets.add((Ticket) entry);
            }
        }
        return tickets;
    }

    private Object parse(String[] fields, Map<String, User> usersByDni, Map<String, Group> groupsById,
                         Map<String, User> detachedUsers) {
        LocalDate date = fields.length > 2 ? Values.parseDate(fields[2]) : null;
        if (date == null || fields[1].isBlank()) {
            return null;
        }
        if (GROUP_TICKET.equals(fields[0]) && fields.length == GROUP_FIELDS) {
            return new GroupTicketRecord(fields[1], date, groupsById.get(fields[3]), new ArrayList<>());
        }
        if (INDIVIDUAL_TICKET.equals(fields[0]) && fields.length == INDIVIDUAL_FIELDS) {
            BigDecimal basePrice = Values.parsePrice(fields[3]);
            BigDecimal discount = parseDiscount(fields[4]);
            if (basePrice == null || discount == null || !UserValidator.isValidDni(fields[6])) {
                return null;
            }
            User holder = usersByDni.get(fields[6]);
            if (holder == null) {
                holder = detachedUsers.computeIfAbsent(fields[6],
                        dni -> new User(fields[8], fields[9], dni, fields[7], "", "", ""));
            }
            return new IndividualTicket(fields[1], date, holder, basePrice, discount);
        }
        return null;
    }

    private static BigDecimal parseDiscount(String value) {
        try {
            BigDecimal discount = new BigDecimal(value);
            return discount.signum() >= 0 && discount.compareTo(BigDecimal.ONE) < 0 ? discount : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void warn(int lineNumber) {
        warnings.println("WARNING: skipping invalid ticket record at line " + lineNumber + " of " + file);
    }

    public void save(List<Ticket> tickets) throws ParkException {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                Group group = groupTicket.getGroup();
                lines.add(String.join(SEPARATOR, GROUP_TICKET, groupTicket.getIdentifier(),
                        groupTicket.getDate().toString(), group == null ? "" : group.getIdentifier()));
                for (IndividualTicket member : groupTicket.getComposition()) {
                    lines.add(format(member, groupTicket.getIdentifier()));
                }
            } else if (ticket instanceof IndividualTicket individual) {
                lines.add(format(individual, ""));
            }
        }
        TextFile.writeLines(file, lines);
    }

    private static String format(IndividualTicket ticket, String owner) {
        User user = ticket.getUser();
        return String.join(SEPARATOR, INDIVIDUAL_TICKET, ticket.getIdentifier(), ticket.getDate().toString(),
                ticket.getBasePrice().toPlainString(), ticket.getDiscount().toPlainString(), owner,
                user.getDni(), user.getEmail(), user.getFirstName(), user.getSurnames());
    }
}
