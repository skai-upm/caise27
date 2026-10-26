package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores tickets in a text file, one ticket per line: {@code Id;yyyy-MM-dd;Precio;DNI}.
 */
public class TicketStorage {

    private static final String SEPARATOR = ";";

    private final Path file;

    public TicketStorage(Path file) {
        this.file = file;
    }

    /**
     * @param userByDni resolves the owner's DNI to the registered user, or null if it no longer exists
     */
    public List<Ticket> load(Function<String, User> userByDni) {
        List<Ticket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] f = line.split(SEPARATOR, -1);
                if (f.length == 4 && !f[0].isBlank()) {
                    parse(f, userByDni, tickets);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read ticket file " + file, e);
        }
        return tickets;
    }

    private static void parse(String[] f, Function<String, User> userByDni, List<Ticket> tickets) {
        try {
            tickets.add(new Ticket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[2]), f[3], userByDni.apply(f[3])));
        } catch (DateTimeParseException | NumberFormatException e) {
            // malformed line: skipped
        }
    }

    public void save(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            lines.add(String.join(SEPARATOR, t.getId(), t.getDate().toString(), t.getPrice().toPlainString(),
                t.getOwnerDni()));
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
