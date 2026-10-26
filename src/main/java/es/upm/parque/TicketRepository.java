package es.upm.parque;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
 * Stores tickets in a text file, one per line: {@code id;date;price;dni;email;name;surname}.
 * The holder's identity is kept so tickets of deleted users remain as history.
 */
public class TicketRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 7;

    private final Path file;

    public TicketRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all tickets. Holders are resolved by DNI; a holder that is no longer registered is rebuilt
     * from the stored identity. Malformed lines are skipped.
     */
    public List<Ticket> loadAll(Function<String, User> userByDni) throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Ticket ticket = parse(line, userByDni);
                if (ticket != null) {
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    private static Ticket parse(String line, Function<String, User> userByDni) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != FIELD_COUNT) {
            return null;
        }
        try {
            User user = userByDni.apply(fields[3]);
            if (user == null) {
                user = new User(fields[5], fields[6], fields[3], fields[4], "", "", "");
            }
            return new Ticket(fields[0], LocalDate.parse(fields[1]), user, new BigDecimal(fields[2]));
        } catch (RuntimeException e) {
            return null;
        }
    }

    public void saveAll(Collection<Ticket> tickets) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Ticket ticket : tickets) {
                User user = ticket.getUser();
                writer.write(String.join(SEPARATOR, ticket.getId(), ticket.getDate().toString(),
                        ticket.getPrice().toPlainString(), user.getDni(), user.getEmail(),
                        user.getName(), user.getSurname()));
                writer.newLine();
            }
        }
    }
}
