package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.TicketRepository;

/**
 * Saves tickets to and loads tickets from one text file. Each line starts with a type tag:
 * <pre>
 * I;id;date;price;dni;email;name;surname           individual ticket
 * G;id;date;price;groupId;groupName                group ticket
 * C;parentId;id;date;price;dni;email;name;surname  ticket owned by group ticket parentId
 * </pre>
 * The holder's details are kept so historical tickets survive deletion of their user.
 */
public class TicketFileRepository implements TicketRepository {

    private final Path file;

    public TicketFileRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all top-level tickets. Holders are resolved by DNI and groups by identifier;
     * when they no longer exist, detached objects are rebuilt from the stored details.
     * Malformed lines are skipped.
     */
    @Override
    public List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById)
            throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        Map<String, String[]> groupLines = new LinkedHashMap<>();
        Map<String, List<IndividualTicket>> constituents = new LinkedHashMap<>();
        List<Object> order = new ArrayList<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] f = line.split(";", -1);
            try {
                if (f[0].equals("I") && f.length == 8) {
                    IndividualTicket t = new IndividualTicket(f[1], LocalDate.parse(f[2]), new BigDecimal(f[3]),
                            holder(userByDni, f[4], f[5], f[6], f[7]));
                    order.add(t);
                } else if (f[0].equals("G") && f.length == 6) {
                    LocalDate.parse(f[2]);
                    new BigDecimal(f[3]);
                    groupLines.put(f[1], f);
                    constituents.put(f[1], new ArrayList<>());
                    order.add(f[1]);
                } else if (f[0].equals("C") && f.length == 9 && constituents.containsKey(f[1])) {
                    constituents.get(f[1]).add(new IndividualTicket(f[2], LocalDate.parse(f[3]),
                            new BigDecimal(f[4]), holder(userByDni, f[5], f[6], f[7], f[8])));
                }
            } catch (DateTimeParseException | NumberFormatException e) {
                // skip malformed line
            }
        }
        for (Object entry : order) {
            if (entry instanceof Ticket t) {
                tickets.add(t);
            } else {
                String[] f = groupLines.get((String) entry);
                Group group = groupById.apply(f[4]);
                if (group == null) {
                    group = new Group(f[4], f[5]);
                }
                tickets.add(new GroupTicket(f[1], LocalDate.parse(f[2]), new BigDecimal(f[3]), group,
                        constituents.get(f[1])));
            }
        }
        return tickets;
    }

    @Override
    public void save(List<Ticket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof GroupTicket gt) {
                lines.add(String.join(";", "G", gt.getId(), gt.getDate().toString(),
                        gt.getPrice().toPlainString(), gt.getGroup().getId(), gt.getGroup().getName()));
                for (IndividualTicket c : gt.getTickets()) {
                    lines.add("C;" + gt.getId() + ";" + individualFields(c));
                }
            } else if (t instanceof IndividualTicket it) {
                lines.add("I;" + individualFields(it));
            }
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private static String individualFields(IndividualTicket t) {
        User u = t.getUser();
        return String.join(";", t.getId(), t.getDate().toString(), t.getPrice().toPlainString(),
                u.getDni(), u.getEmail(), u.getName(), u.getSurname());
    }

    private static User holder(Function<String, User> userByDni, String dni, String email,
                               String name, String surname) {
        User user = userByDni.apply(dni);
        return user != null ? user : new User(name, surname, dni, email, "", "", "");
    }
}
