package com.parque.persistence;

import com.parque.business.model.Group;
import com.parque.business.model.GroupTicket;
import com.parque.business.model.IndividualTicket;
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
 * Stores group tickets, one per line: {@code Identificador;Grupo;yyyy-MM-dd;PrecioUnitario;PrecioTotal;Id1,Id2,...}.
 * Constituent individual tickets are stored by the {@link TicketRepository} and referenced here by identifier.
 */
public class GroupTicketRepository {
    private static final String SEPARATOR = ";";
    private static final String LIST_SEPARATOR = ",";

    private final Path file;

    public GroupTicketRepository(Path file) {
        this.file = file;
    }

    public List<GroupTicket> load(Function<String, Group> groupById, Function<String, IndividualTicket> ticketById)
            throws IOException {
        List<GroupTicket> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            try {
                if (f.length != 6) {
                    throw new IllegalArgumentException("wrong number of fields");
                }
                List<IndividualTicket> constituents = new ArrayList<>();
                if (!f[5].isEmpty()) {
                    for (String id : f[5].split(LIST_SEPARATOR)) {
                        IndividualTicket t = ticketById.apply(id);
                        if (t == null) {
                            System.err.println("Ignoring unknown constituent ticket " + id + " of " + f[0]);
                        } else {
                            constituents.add(t);
                        }
                    }
                }
                result.add(new GroupTicket(f[0], groupById.apply(f[1]), f[1], LocalDate.parse(f[2]),
                        new BigDecimal(f[3]), new BigDecimal(f[4]), constituents));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                System.err.println("Skipping malformed group ticket line " + lineNumber + " in " + file);
            }
        }
        return result;
    }

    public void save(List<GroupTicket> groupTickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (GroupTicket g : groupTickets) {
            List<String> ids = new ArrayList<>();
            for (IndividualTicket t : g.getConstituents()) {
                ids.add(t.getId());
            }
            lines.add(String.join(SEPARATOR, g.getId(), g.getGroupId(), g.getDate().toString(),
                    g.getUnitPrice().toPlainString(), g.getPriceText(), String.join(LIST_SEPARATOR, ids)));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
