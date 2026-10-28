package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Stores tickets. Individual tickets: {@code I;id;date;price;dni;email}. Group
 * tickets: {@code G;id;date;groupId} ({@code -} when the group no longer exists)
 * followed by one {@code C;id;price;dni;email} line per ticket of its composition.
 * The user's DNI and e-mail are kept so historical tickets survive user deletion.
 */
public class TicketRepository extends TextFileRepository {

    private static final String NO_GROUP = "-";

    public TicketRepository(Path file) {
        super(file);
    }

    public void save(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                Group group = groupTicket.getGroup();
                lines.add(join("G", ticket.getIdentifier(), ticket.getDate().toString(),
                        group == null ? NO_GROUP : group.getIdentifier()));
                for (IndividualTicket part : ticket.getIndividualTickets()) {
                    lines.add(join("C", part.getIdentifier(), part.getUnitPrice().toPlainString(),
                            part.getUser().getDni(), part.getUser().getEmail()));
                }
            } else if (ticket instanceof IndividualTicket individual) {
                lines.add(join("I", ticket.getIdentifier(), ticket.getDate().toString(),
                        individual.getUnitPrice().toPlainString(),
                        individual.getUser().getDni(), individual.getUser().getEmail()));
            }
        }
        writeLines(lines);
    }

    /**
     * Loads tickets.
     *
     * @param groups       resolves a group identifier among the existing groups
     * @param userResolver resolves {@code (dni, email)} to the active user or a historical user record
     */
    public List<Ticket> load(Function<String, Optional<Group>> groups, BiFunction<String, String, User> userResolver) {
        List<Ticket> tickets = new ArrayList<>();
        List<String> lines = readLines();
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            int lineNumber = ++i;
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            try {
                if (f[0].equals("I") && f.length == 6) {
                    tickets.add(new IndividualTicket(f[1], LocalDate.parse(f[2]), userResolver.apply(f[4], f[5]),
                            new BigDecimal(f[3])));
                } else if (f[0].equals("G") && f.length == 4) {
                    List<IndividualTicket> composition = new ArrayList<>();
                    while (i < lines.size() && lines.get(i).startsWith("C" + SEPARATOR)) {
                        String[] c = fields(lines.get(i++));
                        if (c.length != 5) {
                            throw new InvalidValueException("malformed composition line");
                        }
                        composition.add(new IndividualTicket(c[1], LocalDate.parse(f[2]),
                                userResolver.apply(c[3], c[4]), new BigDecimal(c[2])));
                    }
                    Group group = f[3].equals(NO_GROUP) ? null : groups.apply(f[3]).orElse(null);
                    tickets.add(GroupTicket.restore(f[1], LocalDate.parse(f[2]), group, composition));
                } else {
                    throw new InvalidValueException("unknown record");
                }
            } catch (RuntimeException e) {
                problem(lineNumber, line, e.getMessage());
            }
        }
        return tickets;
    }
}
