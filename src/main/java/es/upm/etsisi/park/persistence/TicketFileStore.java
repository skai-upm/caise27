package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.GroupTicket;
import es.upm.etsisi.park.model.IndividualTicket;
import es.upm.etsisi.park.model.Ticket;
import es.upm.etsisi.park.model.User;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores tickets, one record per line:
 * <pre>
 * INDIVIDUAL;id;date;basePrice;discount;dni;email;firstName;surnames
 * GROUP;id;date;groupId|-
 * MEMBER;groupTicketId;id;basePrice;discount;dni;email;firstName;surnames
 * </pre>
 * The holder's identity is stored with every individual ticket so that historical tickets
 * of deleted users can still be rebuilt.
 */
public class TicketFileStore {

    private static final String SEPARATOR = ";";
    private static final String INDIVIDUAL = "INDIVIDUAL";
    private static final String GROUP = "GROUP";
    private static final String MEMBER = "MEMBER";
    private static final String NO_GROUP = "-";
    private static final int INDIVIDUAL_FIELDS = 9;
    private static final int GROUP_FIELDS = 4;
    private static final int MEMBER_FIELDS = 9;

    private final Path file;

    public TicketFileStore(Path file) {
        this.file = file;
    }

    public List<Ticket> load(Function<String, Optional<User>> userByDni, Function<String, Optional<Group>> groupById) {
        List<Ticket> tickets = new ArrayList<>();
        Map<String, String[]> groupHeaders = new LinkedHashMap<>();
        Map<String, List<IndividualTicket>> groupMembers = new LinkedHashMap<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = line.split(SEPARATOR, -1);
            try {
                if (INDIVIDUAL.equals(f[0]) && f.length == INDIVIDUAL_FIELDS) {
                    tickets.add(individual(f[1], LocalDate.parse(f[2]), f, 3, userByDni));
                } else if (GROUP.equals(f[0]) && f.length == GROUP_FIELDS) {
                    groupHeaders.put(f[1], f);
                    groupMembers.put(f[1], new ArrayList<>());
                } else if (MEMBER.equals(f[0]) && f.length == MEMBER_FIELDS && groupHeaders.containsKey(f[1])) {
                    LocalDate date = LocalDate.parse(groupHeaders.get(f[1])[2]);
                    groupMembers.get(f[1]).add(individual(f[2], date, f, 3, userByDni));
                } else {
                    TextFiles.warnMalformed(file, line);
                }
            } catch (DateTimeParseException | NumberFormatException e) {
                TextFiles.warnMalformed(file, line);
            }
        }
        for (Map.Entry<String, String[]> entry : groupHeaders.entrySet()) {
            String[] header = entry.getValue();
            Group group = NO_GROUP.equals(header[3]) ? null : groupById.apply(header[3]).orElse(null);
            tickets.add(new GroupTicket(header[1], LocalDate.parse(header[2]), group, groupMembers.get(entry.getKey())));
        }
        return tickets;
    }

    /** Builds an individual ticket from fields {@code basePrice;discount;dni;email;firstName;surnames} at {@code from}. */
    private static IndividualTicket individual(String id, LocalDate date, String[] f, int from,
                                               Function<String, Optional<User>> userByDni) {
        BigDecimal basePrice = new BigDecimal(f[from]);
        int discount = Integer.parseInt(f[from + 1]);
        String dni = f[from + 2];
        User user = userByDni.apply(dni)
                .orElseGet(() -> new User(f[from + 4], f[from + 5], dni, f[from + 3], "", "", ""));
        return new IndividualTicket(id, date, user, basePrice, discount);
    }

    public void save(List<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                Group group = groupTicket.getGroup();
                lines.add(String.join(SEPARATOR, GROUP, ticket.getId(), ticket.getDate().toString(),
                        group == null ? NO_GROUP : group.getId()));
                for (IndividualTicket member : groupTicket.getTickets()) {
                    lines.add(MEMBER + SEPARATOR + groupTicket.getId() + SEPARATOR + member.getId()
                            + SEPARATOR + holderFields(member));
                }
            } else if (ticket instanceof IndividualTicket individual) {
                lines.add(String.join(SEPARATOR, INDIVIDUAL, ticket.getId(), ticket.getDate().toString(),
                        holderFields(individual)));
            }
        }
        TextFiles.writeLines(file, lines);
    }

    private static String holderFields(IndividualTicket ticket) {
        User user = ticket.getUser();
        return String.join(SEPARATOR, ticket.getBasePrice().toPlainString(),
                String.valueOf(ticket.getDiscountPercent()), user.getDni(), user.getEmail(),
                user.getFirstName(), user.getSurnames());
    }
}
