package es.upm.etsisi.amusementpark;

import java.io.IOException;
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
 * Stores tickets in a text file, one ticket per line: {@code Id;yyyy-MM-dd;DNI;Precio}.
 * Owners are stored by DNI and linked back to existing users on load.
 */
public class TicketFileRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 4;

    private final Path file;

    public TicketFileRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all tickets; a missing file means there are no tickets.
     *
     * @param userByDni returns the existing user with a DNI, or {@code null} if none
     */
    public List<Ticket> load(Function<String, User> userByDni) throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            try {
                if (f.length != FIELD_COUNT) {
                    throw new IllegalArgumentException("wrong number of fields");
                }
                LocalDate date = LocalDate.parse(f[1]);
                BigDecimal price = new BigDecimal(f[3]);
                tickets.add(new Ticket(f[0], date, f[2], userByDni.apply(f[2]), price));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                System.err.println("Skipping malformed ticket record at line " + lineNumber + " of " + file);
            }
        }
        return tickets;
    }

    /** Replaces the file content with the given tickets. */
    public void save(Collection<Ticket> tickets) throws IOException {
        List<String> lines = new ArrayList<>(tickets.size());
        for (Ticket t : tickets) {
            lines.add(String.join(SEPARATOR, t.getId(), t.getDate().toString(), t.getOwnerDni(),
                    t.getPrice().toPlainString()));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
