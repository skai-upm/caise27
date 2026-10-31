package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.TicketRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores the ticket aggregate in three files:
 * <ul>
 *   <li>tickets: {@code id;date;price;ownerGroupTicketId;dni;email;name;surname;specialPassIssued}
 *       (owner empty for stand-alone tickets; the last column is optional for E2 files);</li>
 *   <li>group tickets: {@code id;date;price;groupId;groupName};</li>
 *   <li>passes: {@code passId;kind;ticketId;price;consumed} with kind {@code SPEED} or {@code SPECIAL}.</li>
 * </ul>
 * The holder's email and name are kept so that historical tickets survive the
 * deletion of their user; a deleted group is likewise rebuilt from its id and name.
 */
public class FileTicketRepository implements TicketRepository {

    private static final int E2_TICKET_FIELDS = 8;
    private static final int TICKET_FIELDS = 9;
    private static final int GROUP_TICKET_FIELDS = 5;
    private static final int PASS_FIELDS = 5;
    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final StoragePaths paths;

    public FileTicketRepository(StoragePaths paths) {
        this.paths = paths;
    }

    @Override
    public Tickets loadAll(Function<String, User> userByDni, Function<String, Group> groupById)
            throws IOException {
        List<IndividualTicket> individual = new ArrayList<>();
        Map<String, IndividualTicket> byId = new HashMap<>();
        Map<String, List<IndividualTicket>> constituents = new LinkedHashMap<>();
        for (String[] f : TextFileStore.readRecords(paths.tickets(), E2_TICKET_FIELDS, TICKET_FIELDS)) {
            User user = userByDni.apply(f[4]);
            if (user == null) {
                user = new User(f[6], f[7], f[4], f[5], "", "", "");
            }
            IndividualTicket ticket = new IndividualTicket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[2]), user);
            if (f.length == TICKET_FIELDS && Boolean.parseBoolean(f[8])) {
                ticket.markSpecialPassIssued();
            }
            byId.put(ticket.getId(), ticket);
            if (f[3].isEmpty()) {
                individual.add(ticket);
            } else {
                constituents.computeIfAbsent(f[3], k -> new ArrayList<>()).add(ticket);
            }
        }
        loadPasses(byId);

        List<GroupTicket> group = new ArrayList<>();
        for (String[] f : TextFileStore.readRecords(paths.groupTickets(), GROUP_TICKET_FIELDS)) {
            Group owner = groupById.apply(f[3]);
            if (owner == null) {
                owner = new Group(f[3], f[4]);
            }
            group.add(new GroupTicket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[2]), owner,
                    constituents.getOrDefault(f[0], List.of())));
        }
        return new Tickets(individual, group);
    }

    private void loadPasses(Map<String, IndividualTicket> tickets) throws IOException {
        for (String[] f : TextFileStore.readRecords(paths.passes(), PASS_FIELDS)) {
            IndividualTicket ticket = tickets.get(f[2]);
            if (ticket == null) {
                System.err.println("WARNING: pass " + f[0] + " references unknown ticket " + f[2]);
                continue;
            }
            BigDecimal price = new BigDecimal(f[3]);
            boolean consumed = Boolean.parseBoolean(f[4]);
            if (SPEED.equals(f[1])) {
                ticket.addSpeedPass(new SpeedPass(f[0], price, consumed));
            } else if (SPECIAL.equals(f[1])) {
                ticket.setSpecialPass(new SpecialPass(f[0], price, consumed));
            } else {
                System.err.println("WARNING: pass " + f[0] + " has unknown kind " + f[1]);
            }
        }
    }

    @Override
    public void saveAll(List<IndividualTicket> individual, List<GroupTicket> group) throws IOException {
        List<String[]> ticketRecords = new ArrayList<>();
        List<String[]> groupRecords = new ArrayList<>();
        List<String[]> passRecords = new ArrayList<>();
        for (IndividualTicket ticket : individual) {
            addTicket(ticket, "", ticketRecords, passRecords);
        }
        for (GroupTicket groupTicket : group) {
            for (IndividualTicket ticket : groupTicket.getTickets()) {
                addTicket(ticket, groupTicket.getId(), ticketRecords, passRecords);
            }
            groupRecords.add(new String[] {groupTicket.getId(), groupTicket.getDate().toString(),
                    groupTicket.getPrice().toPlainString(), groupTicket.getGroup().getId(),
                    groupTicket.getGroup().getName()});
        }
        TextFileStore.writeRecords(paths.tickets(), ticketRecords);
        TextFileStore.writeRecords(paths.groupTickets(), groupRecords);
        TextFileStore.writeRecords(paths.passes(), passRecords);
    }

    private static void addTicket(IndividualTicket ticket, String owner, List<String[]> tickets,
                                  List<String[]> passes) {
        User user = ticket.getUser();
        tickets.add(new String[] {ticket.getId(), ticket.getDate().toString(), ticket.getPrice().toPlainString(),
                owner, user.getDni(), user.getEmail(), user.getName(), user.getSurname(),
                String.valueOf(ticket.isSpecialPassIssued())});
        for (Pass pass : ticket.getPasses()) {
            passes.add(new String[] {pass.getId(), pass instanceof SpecialPass ? SPECIAL : SPEED, ticket.getId(),
                    pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())});
        }
    }
}
