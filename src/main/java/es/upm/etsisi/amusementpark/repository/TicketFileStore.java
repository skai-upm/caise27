package es.upm.etsisi.amusementpark.repository;

import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.GroupTicket;
import es.upm.etsisi.amusementpark.model.IndividualTicket;
import es.upm.etsisi.amusementpark.model.Ticket;
import es.upm.etsisi.amusementpark.model.User;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores tickets in a text file. Each individual ticket keeps a snapshot of its user so that
 * historical tickets can be rebuilt after the user has been deleted.
 *
 * <pre>
 * INDIVIDUAL;id;date;basePrice;discount;userState;dni;firstName;surnames;email
 * GROUP;id;date;groupId
 * MEMBER;groupTicketId;id;date;basePrice;discount;userState;dni;firstName;surnames;email
 * </pre>
 * userState is ACTIVE when the user was registered at save time and HISTORIC otherwise.
 */
public class TicketFileStore {

    private static final String INDIVIDUAL = "INDIVIDUAL";
    private static final String GROUP = "GROUP";
    private static final String MEMBER = "MEMBER";
    private static final String ACTIVE = "ACTIVE";
    private static final String HISTORIC = "HISTORIC";
    private static final int INDIVIDUAL_FIELDS = 9;

    private final LineFile file;
    private final UserRepository users;
    private final GroupRepository groups;

    public TicketFileStore(Path file, UserRepository users, GroupRepository groups) {
        this.file = new LineFile(file);
        this.users = users;
        this.groups = groups;
    }

    public List<Ticket> load() {
        List<Ticket> tickets = new ArrayList<>();
        Map<String, String[]> groupHeaders = new LinkedHashMap<>();
        Map<String, List<IndividualTicket>> compositions = new LinkedHashMap<>();
        for (String[] f : file.readRecords()) {
            try {
                switch (f[0]) {
                    case INDIVIDUAL -> tickets.add(parseIndividual(f, 1));
                    case GROUP -> {
                        groupHeaders.put(f[1], f);
                        compositions.put(f[1], new ArrayList<>());
                    }
                    case MEMBER -> compositions.get(f[1]).add(parseIndividual(f, 2));
                    default -> throw new IllegalArgumentException("Unknown record type");
                }
            } catch (RuntimeException e) {
                file.warnMalformed(f);
            }
        }
        groupHeaders.forEach((id, f) -> {
            Group group = f[3].isEmpty() ? null : groups.findById(f[3]).orElse(null);
            tickets.add(new GroupTicket(id, LocalDate.parse(f[2]), group, compositions.get(id)));
        });
        return tickets;
    }

    public void save(List<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket g) {
                lines.add(LineFile.join(GROUP, g.getId(), g.getDate(), g.getGroup() == null ? "" : g.getGroup().getId()));
                for (IndividualTicket member : g.getIndividualTickets()) {
                    lines.add(MEMBER + LineFile.SEPARATOR + g.getId() + LineFile.SEPARATOR + format(member));
                }
            } else if (ticket instanceof IndividualTicket i) {
                lines.add(INDIVIDUAL + LineFile.SEPARATOR + format(i));
            }
        }
        file.writeLines(lines);
    }

    private String format(IndividualTicket t) {
        User u = t.getUser();
        return LineFile.join(t.getId(), t.getDate(), t.getBasePrice(), t.getDiscountPercent(),
                users.isRegistered(u) ? ACTIVE : HISTORIC,
                u.getDni(), u.getFirstName(), u.getSurnames(), u.getEmail());
    }

    private IndividualTicket parseIndividual(String[] f, int offset) {
        if (f.length != offset + INDIVIDUAL_FIELDS) {
            throw new IllegalArgumentException("Wrong number of fields");
        }
        String dni = f[offset + 5];
        User user = ACTIVE.equals(f[offset + 4]) ? users.findByDni(dni).orElse(null) : null;
        if (user == null) {
            user = new User(f[offset + 6], f[offset + 7], dni, f[offset + 8], "", "", "");
        }
        return new IndividualTicket(f[offset], LocalDate.parse(f[offset + 1]), user,
                new BigDecimal(f[offset + 2]), Integer.parseInt(f[offset + 3]));
    }
}
