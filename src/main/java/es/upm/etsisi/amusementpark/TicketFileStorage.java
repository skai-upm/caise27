package es.upm.etsisi.amusementpark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores tickets in a text file, one per line: {@code Id;DNI;yyyy-MM-dd;Precio}. Owners are stored by DNI
 * and linked to the loaded users; tickets of users that no longer exist are kept as history.
 * This is the only class that touches the ticket file.
 */
public class TicketFileStorage {

    public static final String DEFAULT_FILE = "tickets.txt";
    private static final String SEPARATOR = ";";
    private static final int FIELDS = 4;

    private final Path file;

    public TicketFileStorage(Path file) {
        this.file = file;
    }

    public List<Ticket> load(List<User> users) {
        List<Ticket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read ticket file " + file, e);
        }
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            try {
                if (f.length != FIELDS) {
                    throw new IllegalArgumentException("wrong number of fields");
                }
                LocalDate date = LocalDate.parse(f[2]);
                BigDecimal price = new BigDecimal(f[3]);
                Optional<User> owner = users.stream().filter(u -> u.getDni().equals(f[1])).findFirst();
                tickets.add(owner.map(u -> new Ticket(f[0], u, date, price))
                        .orElseGet(() -> new Ticket(f[0], f[1], date, price)));
            } catch (IllegalArgumentException | DateTimeParseException | ArithmeticException e) {
                System.err.println("Skipping malformed ticket record: " + line);
            }
        }
        return tickets;
    }

    public void save(List<Ticket> tickets) {
        List<String> lines = new ArrayList<>(tickets.size());
        for (Ticket t : tickets) {
            lines.add(String.join(SEPARATOR, t.getId(), t.getOwnerDni(), t.getDate().toString(),
                    t.getPrice().toPlainString()));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write ticket file " + file, e);
        }
    }
}
