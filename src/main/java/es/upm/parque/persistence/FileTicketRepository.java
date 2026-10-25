package es.upm.parque.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.User;

/**
 * Stores tickets in three files: individual tickets as
 * {@code id;date;price;userDni;groupTicketId;specialPassPurchased} (empty group ticket for tickets bought
 * individually), group tickets as {@code id;date;groupId;total}, and passes as
 * {@code id;SPEED|SPECIAL;ticketId;price;consumed}. Saving writes passes first, then tickets, then group
 * tickets, so removed children disappear before their parents.
 */
public class FileTicketRepository implements TicketRepository {

    private static final String SEPARATOR = ";";
    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final TextFileStore ticketStore;
    private final TextFileStore groupTicketStore;
    private final TextFileStore passStore;

    public FileTicketRepository(Path ticketsFile, Path groupTicketsFile, Path passesFile) {
        this.ticketStore = new TextFileStore(ticketsFile);
        this.groupTicketStore = new TextFileStore(groupTicketsFile);
        this.passStore = new TextFileStore(passesFile);
    }

    @Override
    public Loaded load(Function<String, User> userByDni, Function<String, Group> groupById) throws IOException {
        Map<String, String[]> groupTicketFields = new LinkedHashMap<>();
        Map<String, List<IndividualTicket>> internalTickets = new LinkedHashMap<>();
        for (String line : groupTicketStore.readLines()) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != 4) {
                groupTicketStore.warnSkipped(line, "expected 4 fields");
                continue;
            }
            groupTicketFields.put(f[0], f);
            internalTickets.put(f[0], new ArrayList<>());
        }

        Map<String, IndividualTicket> ticketsById = new HashMap<>();
        List<IndividualTicket> individualTickets = new ArrayList<>();
        for (String line : ticketStore.readLines()) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != 5 && f.length != 6) {
                ticketStore.warnSkipped(line, "expected 5 or 6 fields");
                continue;
            }
            User user = userByDni.apply(f[3]);
            if (user == null) {
                ticketStore.warnSkipped(line, "unknown user " + f[3]);
                continue;
            }
            IndividualTicket ticket;
            try {
                boolean specialPassPurchased = f.length == 6 && Boolean.parseBoolean(f[5]);
                ticket = new IndividualTicket(f[0], LocalDate.parse(f[1]), user, new BigDecimal(f[2]),
                        specialPassPurchased);
            } catch (DateTimeParseException | NumberFormatException e) {
                ticketStore.warnSkipped(line, "invalid date or price");
                continue;
            }
            if (f[4].isEmpty()) {
                individualTickets.add(ticket);
            } else if (internalTickets.containsKey(f[4])) {
                internalTickets.get(f[4]).add(ticket);
            } else {
                ticketStore.warnSkipped(line, "unknown group ticket " + f[4]);
                continue;
            }
            ticketsById.put(ticket.getId(), ticket);
        }

        List<GroupTicket> groupTickets = new ArrayList<>();
        for (String[] f : groupTicketFields.values()) {
            String line = String.join(SEPARATOR, f);
            Group group = groupById.apply(f[2]);
            if (group == null) {
                groupTicketStore.warnSkipped(line, "unknown group " + f[2]);
                continue;
            }
            try {
                groupTickets.add(new GroupTicket(f[0], LocalDate.parse(f[1]), group, new BigDecimal(f[3]),
                        internalTickets.get(f[0])));
            } catch (DateTimeParseException | NumberFormatException e) {
                groupTicketStore.warnSkipped(line, "invalid date or price");
            }
        }

        loadPasses(ticketsById);
        return new Loaded(individualTickets, groupTickets);
    }

    private void loadPasses(Map<String, IndividualTicket> ticketsById) throws IOException {
        for (String line : passStore.readLines()) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != 5) {
                passStore.warnSkipped(line, "expected 5 fields");
                continue;
            }
            IndividualTicket ticket = ticketsById.get(f[2]);
            if (ticket == null) {
                passStore.warnSkipped(line, "unknown ticket " + f[2]);
                continue;
            }
            BigDecimal price;
            try {
                price = new BigDecimal(f[3]);
            } catch (NumberFormatException e) {
                passStore.warnSkipped(line, "invalid price");
                continue;
            }
            boolean consumed = Boolean.parseBoolean(f[4]);
            if (SPEED.equals(f[1])) {
                ticket.addSpeedPass(new SpeedPass(f[0], price, consumed));
            } else if (SPECIAL.equals(f[1])) {
                ticket.attachSpecialPass(new SpecialPass(f[0], price, consumed));
            } else {
                passStore.warnSkipped(line, "unknown pass type " + f[1]);
            }
        }
    }

    @Override
    public void saveAll(List<IndividualTicket> individualTickets, List<GroupTicket> groupTickets)
            throws IOException {
        List<String> ticketLines = new ArrayList<>();
        List<String> passLines = new ArrayList<>();
        for (IndividualTicket ticket : individualTickets) {
            addLines(ticket, "", ticketLines, passLines);
        }
        List<String> groupLines = new ArrayList<>();
        for (GroupTicket groupTicket : groupTickets) {
            groupLines.add(String.join(SEPARATOR, groupTicket.getId(), groupTicket.getDate().toString(),
                    groupTicket.getGroup().getId(), groupTicket.getPrice().toPlainString()));
            for (IndividualTicket ticket : groupTicket.getTickets()) {
                addLines(ticket, groupTicket.getId(), ticketLines, passLines);
            }
        }
        passStore.writeLines(passLines);
        ticketStore.writeLines(ticketLines);
        groupTicketStore.writeLines(groupLines);
    }

    private static void addLines(IndividualTicket ticket, String groupTicketId, List<String> ticketLines,
            List<String> passLines) {
        ticketLines.add(String.join(SEPARATOR, ticket.getId(), ticket.getDate().toString(),
                ticket.getPrice().toPlainString(), ticket.getUser().getDni(), groupTicketId,
                String.valueOf(ticket.isSpecialPassPurchased())));
        for (Pass pass : ticket.getPasses()) {
            String type = pass instanceof SpecialPass ? SPECIAL : SPEED;
            passLines.add(String.join(SEPARATOR, pass.getId(), type, ticket.getId(),
                    pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())));
        }
    }
}
