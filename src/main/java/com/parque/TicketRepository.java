package com.parque;

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
 * Stores individual tickets in a text file, one per line: {@code Identificador;DNI;yyyy-MM-dd;Precio}.
 */
public class TicketRepository {
    private static final String SEPARATOR = ";";

    private final Path file;

    public TicketRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads every well-formed ticket, resolving the owner's DNI with {@code userByDni}. Tickets whose DNI
     * matches no registered user are kept as history. A missing file means no tickets.
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
                if (f.length != 4) {
                    throw new IllegalArgumentException("wrong number of fields");
                }
                tickets.add(new Ticket(f[0], userByDni.apply(f[1]), f[1], LocalDate.parse(f[2]), new BigDecimal(f[3])));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                System.err.println("Skipping malformed ticket line " + lineNumber + " in " + file);
            }
        }
        return tickets;
    }

    /** Rewrites the file with exactly the given tickets. */
    public void save(List<Ticket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            lines.add(String.join(SEPARATOR, t.getId(), t.getHolderDni(), t.getDate().toString(), t.getPriceText()));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
