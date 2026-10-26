package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stores individual tickets in a text file, one per line: {@code id;dni;yyyy-MM-dd;price}.
 */
public class TicketRepository {

    private final Path file;

    public TicketRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads every stored ticket, linking it to the given already-loaded users by DNI.
     * Lines whose user is unknown or that cannot be parsed are skipped.
     */
    public List<Ticket> loadAll(Map<String, User> usersByDni) {
        List<Ticket> tickets = new ArrayList<>();
        if (!Files.exists(file)) {
            return tickets;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                Ticket ticket = parse(line, usersByDni);
                if (ticket != null) {
                    tickets.add(ticket);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read tickets file " + file, e);
        }
        return tickets;
    }

    public void saveAll(List<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            lines.add(String.join(";", t.getId(), t.getUser().getDni(),
                    MainController.formatDate(t.getDate()), MainController.formatPrice(t.getPrice())));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write tickets file " + file, e);
        }
    }

    private static Ticket parse(String line, Map<String, User> usersByDni) {
        String[] f = line.split(";", -1);
        if (f.length != 4 || !usersByDni.containsKey(f[1])) {
            return null;
        }
        try {
            return new Ticket(f[0], MainController.parseDate(f[2]), usersByDni.get(f[1]), new BigDecimal(f[3]));
        } catch (ParkException | NumberFormatException e) {
            return null;
        }
    }
}
