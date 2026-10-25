package es.upm.parque.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;

/**
 * Stores group tickets in a text file, one per line as
 * id;yyyy-MM-dd;groupId;discount;dni:price,dni:price,...
 */
public class TextFileGroupTicketRepository implements GroupTicketRepository {

    private static final String SEPARATOR = ";";
    private static final String TICKET_SEPARATOR = ",";
    private static final String PRICE_SEPARATOR = ":";
    private static final int FIELDS = 5;

    private final Path file;

    public TextFileGroupTicketRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads the stored group tickets. Unknown groups and users become detached historical records.
     */
    @Override
    public List<GroupTicket> load(Function<String, Group> groupById, Function<String, User> userByDni)
            throws IOException {
        List<GroupTicket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            GroupTicket ticket = parse(line.split(SEPARATOR, -1), groupById, userByDni);
            if (ticket != null) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    @Override
    public void save(Collection<GroupTicket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (GroupTicket ticket : tickets) {
            List<String> members = new ArrayList<>();
            for (IndividualTicket member : ticket.getTickets()) {
                members.add(member.getHolder().getDni() + PRICE_SEPARATOR + member.getPrice().toPlainString());
            }
            lines.add(String.join(SEPARATOR, ticket.getId(), SystemDate.format(ticket.getDate()),
                    ticket.getGroup().getId(), String.valueOf(ticket.getDiscountPercent()),
                    String.join(TICKET_SEPARATOR, members)));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private static GroupTicket parse(String[] parts, Function<String, Group> groupById,
                                     Function<String, User> userByDni) {
        if (parts.length != FIELDS || parts[0].isBlank()) {
            return null;
        }
        LocalDate date = SystemDate.parse(parts[1]);
        Integer discount = parseDiscount(parts[3]);
        if (date == null || discount == null) {
            return null;
        }
        List<User> holders = new ArrayList<>();
        List<BigDecimal> prices = new ArrayList<>();
        for (String member : parts[4].split(TICKET_SEPARATOR)) {
            String[] pair = member.split(PRICE_SEPARATOR, -1);
            BigDecimal price = pair.length == 2 ? Ticket.parsePrice(pair[1]) : null;
            if (price == null) {
                return null;
            }
            User holder = userByDni.apply(pair[0]);
            holders.add(holder != null ? holder : new User("", "", pair[0], "", "", "", ""));
            prices.add(price);
        }
        Group group = groupById.apply(parts[2]);
        return new GroupTicket(parts[0], date, group != null ? group : new Group(parts[2], ""), holders, prices,
                discount);
    }

    private static Integer parseDiscount(String text) {
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
