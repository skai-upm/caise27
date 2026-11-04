package es.upm.park;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores tickets in a text file: {@code id;ownerDni;yyyy-MM-dd;price}, one per line.
 */
public class TicketFileStorage {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 4;

    private final Path file;

    public TicketFileStorage(Path file) {
        this.file = file;
    }

    /**
     * Loads all tickets, linking each to the user returned by {@code userByDni}.
     * Tickets of users that no longer exist are kept without owner reference.
     */
    public List<Ticket> load(Function<String, User> userByDni) {
        List<Ticket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                Ticket ticket = parse(line, userByDni);
                if (ticket != null) {
                    tickets.add(ticket);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read tickets file " + file, e);
        }
        return tickets;
    }

    public void save(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            lines.add(format(t));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write tickets file " + file, e);
        }
    }

    static String format(Ticket t) {
        return String.join(SEPARATOR, t.getId(), t.getOwnerDni(), t.getDate().toString(),
                t.getPrice().toPlainString());
    }

    private static Ticket parse(String line, Function<String, User> userByDni) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] f = line.split(SEPARATOR, -1);
        if (f.length != FIELD_COUNT || f[0].isBlank() || f[1].isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(f[2]);
            BigDecimal price = new BigDecimal(f[3]);
            return new Ticket(f[0], userByDni.apply(f[1]), f[1], date, price);
        } catch (DateTimeParseException | NumberFormatException e) {
            return null;
        }
    }
}
