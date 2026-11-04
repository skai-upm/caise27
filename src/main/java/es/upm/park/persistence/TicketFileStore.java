package es.upm.park.persistence;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.User;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores tickets in a text file, one per line: {@code Id;yyyy-MM-dd;Precio;DNI}. Lines whose
 * DNI matches no registered user are historical records of deleted users and are kept verbatim.
 */
public class TicketFileStore {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 4;

    /** Content of the tickets file: tickets linked to users plus preserved historical lines. */
    public record Contents(List<IndividualTicket> tickets, List<String> historicalLines) {
    }

    private final Path file;

    public TicketFileStore(Path file) {
        this.file = file;
    }

    public Contents load(Function<String, Optional<User>> userByDni) throws IOException {
        List<IndividualTicket> tickets = new ArrayList<>();
        List<String> history = new ArrayList<>();
        if (!Files.exists(file)) {
            return new Contents(tickets, history);
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            IndividualTicket ticket = f.length == FIELD_COUNT ? parse(f, userByDni) : null;
            if (ticket != null) {
                tickets.add(ticket);
            } else if (f.length == FIELD_COUNT && userByDni.apply(f[3]).isEmpty()) {
                history.add(line);
            } else {
                System.err.println("Skipping malformed ticket record at line " + lineNumber + " of " + file);
            }
        }
        return new Contents(tickets, history);
    }

    private static IndividualTicket parse(String[] f, Function<String, Optional<User>> userByDni) {
        Optional<User> user = userByDni.apply(f[3]);
        if (user.isEmpty() || f[0].isBlank()) {
            return null;
        }
        try {
            return new IndividualTicket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[2]), user.get());
        } catch (DateTimeParseException | NumberFormatException e) {
            return null;
        }
    }

    /** Replaces the file content with the tickets followed by the historical lines. */
    public void save(List<IndividualTicket> tickets, List<String> historicalLines) throws IOException {
        List<String> lines = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            lines.add(String.join(SEPARATOR, t.getId(), t.getDate().toString(),
                    t.getPrice().toPlainString(), t.getUser().getDni()));
        }
        lines.addAll(historicalLines);
        Path absolute = file.toAbsolutePath();
        Path dir = absolute.getParent();
        if (dir != null) {
            Files.createDirectories(dir);
        }
        Path tmp = Files.createTempFile(dir, absolute.getFileName().toString(), ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
