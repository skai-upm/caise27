package es.upm.amusementpark.persistence;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.GroupRepository;
import es.upm.amusementpark.repository.UserRepository;

/**
 * Stores tickets. Each ticket keeps the minimum user data (DNI, first name, surnames, email)
 * so historical tickets survive the deletion of their users or groups.
 * <pre>
 * I;id;date;basePrice;dni;firstName;surnames;email
 * G;id;date;basePrice;groupId;memberCount
 * M;dni;firstName;surnames;email        (memberCount lines after each G line)
 * </pre>
 */
public class TicketFileStore {

    private final Path file;

    public TicketFileStore(Path file) {
        this.file = file;
    }

    public List<Ticket> load(UserRepository users, GroupRepository groups) {
        List<Ticket> tickets = new ArrayList<>();
        List<String> lines = TextFiles.read(file);
        int i = 0;
        while (i < lines.size()) {
            String[] f = TextFiles.fields(lines.get(i++));
            try {
                if (f.length == 8 && f[0].equals("I")) {
                    User user = resolve(users, f[4], f[5], f[6], f[7]);
                    tickets.add(new IndividualTicket(f[1], LocalDate.parse(f[2]), new BigDecimal(f[3]), user));
                } else if (f.length == 6 && f[0].equals("G")) {
                    int count = Integer.parseInt(f[5]);
                    List<User> members = new ArrayList<>();
                    for (int m = 0; m < count && i < lines.size(); m++) {
                        String[] mf = TextFiles.fields(lines.get(i++));
                        if (mf.length == 5 && mf[0].equals("M")) {
                            members.add(resolve(users, mf[1], mf[2], mf[3], mf[4]));
                        }
                    }
                    Group group = f[4].isEmpty() ? null : groups.findById(f[4]);
                    if (members.size() == count) {
                        tickets.add(new GroupTicket(f[1], LocalDate.parse(f[2]), new BigDecimal(f[3]), group, members));
                    }
                }
            } catch (RuntimeException e) {
                // malformed record: skipped
            }
        }
        return tickets;
    }

    public void save(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            String base = t.getBasePrice().toPlainString();
            if (t instanceof GroupTicket gt) {
                String groupId = gt.getGroup() == null ? "" : gt.getGroup().getId();
                lines.add(TextFiles.join("G", t.getId(), t.getDate().toString(), base, groupId,
                        String.valueOf(gt.getComposition().size())));
                for (IndividualTicket component : gt.getComposition()) {
                    User u = component.getUser();
                    lines.add(TextFiles.join("M", u.getDni(), u.getFirstName(), u.getSurnames(), u.getEmail()));
                }
            } else if (t instanceof IndividualTicket it) {
                User u = it.getUser();
                lines.add(TextFiles.join("I", t.getId(), t.getDate().toString(), base, u.getDni(), u.getFirstName(),
                        u.getSurnames(), u.getEmail()));
            }
        }
        TextFiles.write(file, lines);
    }

    /** Registered user with that DNI, or a detached snapshot when the user no longer exists. */
    private static User resolve(UserRepository users, String dni, String firstName, String surnames, String email) {
        User user = users.findByDni(dni);
        return user != null ? user : new User(firstName, surnames, dni, email, "", "", "");
    }
}
