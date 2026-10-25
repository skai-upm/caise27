package es.upm.parque;

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

/**
 * Stores tickets in a text file: one ticket per line as id;yyyy-MM-dd;holderDni;price.
 */
public class TicketRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 4;

    private final Path file;

    public TicketRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads the stored tickets. Holders are resolved by DNI; a DNI of a user that no longer exists
     * becomes a detached historical holder so past tickets are kept.
     */
    public List<IndividualTicket> load(Function<String, User> userByDni) throws IOException {
        List<IndividualTicket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            IndividualTicket ticket = parse(line.split(SEPARATOR, -1), userByDni);
            if (ticket != null) {
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    public void save(Collection<IndividualTicket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            lines.add(String.join(SEPARATOR, ticket.getId(), SystemDate.format(ticket.getDate()),
                    ticket.getHolder().getDni(), ticket.getPrice().toPlainString()));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private static IndividualTicket parse(String[] parts, Function<String, User> userByDni) {
        if (parts.length != FIELDS || parts[0].isBlank()) {
            return null;
        }
        LocalDate date = SystemDate.parse(parts[1]);
        BigDecimal price = TicketManager.parsePrice(parts[3]);
        if (date == null || price == null) {
            return null;
        }
        User holder = userByDni.apply(parts[2]);
        if (holder == null) {
            holder = new User("", "", parts[2], "", "", "", "");
        }
        return new IndividualTicket(parts[0], date, holder, price);
    }
}
