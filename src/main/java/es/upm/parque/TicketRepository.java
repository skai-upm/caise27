package es.upm.parque;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Stores tickets in a text file, one ticket per line: {@code Id;yyyy-MM-dd;price;DNI}.
 * Holders are relinked by DNI; a holder that no longer exists is restored as a detached user carrying only its DNI.
 */
public class TicketRepository {
    private static final String SEPARATOR = ";";
    static final String DELETED_USER_NAME = "(deleted user)";

    private final Path file;

    public TicketRepository(Path file) {
        this.file = file;
    }

    public List<Ticket> load(Function<String, User> userByDni) {
        List<Ticket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(SEPARATOR, -1);
                try {
                    if (f.length != 4) {
                        throw new IllegalArgumentException("wrong field count");
                    }
                    tickets.add(new Ticket(f[0], LocalDate.parse(f[1]), holder(f[3], userByDni), new BigDecimal(f[2])));
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    System.err.println("Ignoring malformed ticket line: " + line);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read tickets file " + file + ": " + e.getMessage(), e);
        }
        return tickets;
    }

    public void save(List<Ticket> tickets) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (Ticket t : tickets) {
                    writer.write(String.join(SEPARATOR, t.getId(), t.getDate().toString(), t.getPrice().toPlainString(),
                            t.getUser().getDni()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write tickets file " + file + ": " + e.getMessage(), e);
        }
    }

    private static User holder(String dni, Function<String, User> userByDni) {
        User user = userByDni.apply(dni);
        return user != null ? user : new User(DELETED_USER_NAME, "", dni, "", "", "", "");
    }
}
