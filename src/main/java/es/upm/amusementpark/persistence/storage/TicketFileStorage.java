package es.upm.amusementpark.persistence.storage;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores tickets in a text file. Line kinds:
 * <pre>
 * I;id;date;price;dni;firstName;surnames;email          standalone individual ticket
 * G;id;date;groupId;attached(1|0)                        group ticket
 * C;groupTicketId;id;date;price;dni;firstName;surnames;email   composition ticket of a group ticket
 * </pre>
 * The user snapshot (dni, names, email) allows historical tickets of deleted users to be rebuilt.
 */
public class TicketFileStorage implements TicketStorage {

    private final Path file;

    public TicketFileStorage(Path file) {
        this.file = file;
    }

    @Override
    public List<Ticket> load(UserRepository users, GroupRepository groups) {
        List<Ticket> result = new ArrayList<>();
        Map<String, String[]> groupLines = new LinkedHashMap<>();
        Map<String, List<IndividualTicket>> compositions = new LinkedHashMap<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            try {
                if ("I".equals(f[0]) && f.length == 8) {
                    result.add(individual(f, 1, users));
                } else if ("G".equals(f[0]) && f.length == 5) {
                    groupLines.put(f[1], f);
                    compositions.putIfAbsent(f[1], new ArrayList<>());
                } else if ("C".equals(f[0]) && f.length == 9) {
                    compositions.computeIfAbsent(f[1], k -> new ArrayList<>()).add(individual(f, 2, users));
                }
            } catch (RuntimeException e) {
                // malformed line: ignored
            }
        }
        for (String[] g : groupLines.values()) {
            List<IndividualTicket> composition = compositions.get(g[1]);
            Group group = "1".equals(g[4]) ? groups.findById(g[3]).orElse(null) : null;
            result.addAll(composition);
            result.add(new GroupTicket(g[1], LocalDate.parse(g[2]), g[3], group, composition));
        }
        return result;
    }

    private static IndividualTicket individual(String[] f, int start, UserRepository users) {
        String id = f[start];
        LocalDate date = LocalDate.parse(f[start + 1]);
        BigDecimal price = new BigDecimal(f[start + 2]);
        String dni = f[start + 3];
        User user = users.findByDni(dni)
                .orElseGet(() -> new User(f[start + 4], f[start + 5], dni, f[start + 6], "", "", ""));
        return new IndividualTicket(id, date, user, price);
    }

    @Override
    public void save(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        Set<Ticket> inComposition = tickets.stream().filter(GroupTicket.class::isInstance)
                .flatMap(t -> ((GroupTicket) t).getComposition().stream()).collect(Collectors.toSet());
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket g) {
                lines.add(String.join(TextFiles.SEPARATOR, "G", g.getIdentifier(), g.getDate().toString(),
                        g.getGroupIdentifier(), g.getGroup() != null ? "1" : "0"));
                for (IndividualTicket c : g.getComposition()) {
                    lines.add("C" + TextFiles.SEPARATOR + g.getIdentifier() + TextFiles.SEPARATOR + individualFields(c));
                }
            } else if (t instanceof IndividualTicket i && !inComposition.contains(i)) {
                lines.add("I" + TextFiles.SEPARATOR + individualFields(i));
            }
        }
        TextFiles.writeLines(file, lines);
    }

    private static String individualFields(IndividualTicket t) {
        User u = t.getUser();
        return String.join(TextFiles.SEPARATOR, t.getIdentifier(), t.getDate().toString(), t.getPrice().toPlainString(),
                u.getDni(), u.getFirstName(), u.getSurnames(), u.getEmail());
    }
}
