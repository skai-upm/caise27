package es.upm.parque.persistence;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
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
 * Stores tickets in two text files:
 * individual tickets as {@code Id;yyyy-MM-dd;Precio;DNI} and group tickets as
 * {@code Id;yyyy-MM-dd;IdGrupo;PrecioBase;Descuento;Id-1:DNI,Id-2:DNI,...}.
 */
public class TicketStorage {

    private static final String SEPARATOR = ";";
    private static final String COMPONENT_SEPARATOR = ",";
    private static final String OWNER_SEPARATOR = ":";

    private final Path ticketsFile;
    private final Path groupTicketsFile;

    public TicketStorage(Path ticketsFile, Path groupTicketsFile) {
        this.ticketsFile = ticketsFile;
        this.groupTicketsFile = groupTicketsFile;
    }

    /**
     * @param userByDni resolves a DNI to the registered user, or null if it no longer exists
     * @param groupById resolves a group identifier to the group, or null if it no longer exists
     */
    public List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById) {
        List<Ticket> tickets = new ArrayList<>();
        for (String line : readLines(ticketsFile)) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length == 4 && !f[0].isBlank()) {
                try {
                    tickets.add(new IndividualTicket(f[0], LocalDate.parse(f[1]), new BigDecimal(f[2]), f[3],
                        userByDni.apply(f[3]), null));
                } catch (DateTimeParseException | NumberFormatException e) {
                    // malformed line: skipped
                }
            }
        }
        for (String line : readLines(groupTicketsFile)) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length == 6 && !f[0].isBlank()) {
                try {
                    tickets.add(parseGroupTicket(f, userByDni, groupById));
                } catch (DateTimeParseException | IllegalArgumentException e) {
                    // malformed line: skipped
                }
            }
        }
        return tickets;
    }

    private static GroupTicket parseGroupTicket(String[] f, Function<String, User> userByDni,
            Function<String, Group> groupById) {
        LocalDate date = LocalDate.parse(f[1]);
        BigDecimal basePrice = new BigDecimal(f[3]);
        List<IndividualTicket> components = new ArrayList<>();
        for (String component : f[5].split(COMPONENT_SEPARATOR)) {
            String[] c = component.split(OWNER_SEPARATOR, -1);
            if (c.length != 2) {
                throw new IllegalArgumentException("Malformed group ticket component " + component);
            }
            components.add(new IndividualTicket(c[0], date, basePrice, c[1], userByDni.apply(c[1]), f[0]));
        }
        return new GroupTicket(f[0], date, f[2], groupById.apply(f[2]), basePrice, Integer.parseInt(f[4]),
            components);
    }

    public void save(Collection<Ticket> tickets) {
        List<String> individual = new ArrayList<>();
        List<String> group = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it) {
                individual.add(String.join(SEPARATOR, it.getId(), it.getDate().toString(),
                    it.getPrice().toPlainString(), it.getOwnerDni()));
            } else if (t instanceof GroupTicket gt) {
                List<String> components = new ArrayList<>();
                for (IndividualTicket c : gt.getComponents()) {
                    components.add(c.getId() + OWNER_SEPARATOR + c.getOwnerDni());
                }
                group.add(String.join(SEPARATOR, gt.getId(), gt.getDate().toString(), gt.getGroupId(),
                    gt.getBasePrice().toPlainString(), String.valueOf(gt.getDiscountPercent()),
                    String.join(COMPONENT_SEPARATOR, components)));
            }
        }
        write(ticketsFile, individual);
        write(groupTicketsFile, group);
    }

    private static List<String> readLines(Path file) {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read ticket file " + file, e);
        }
    }

    private static void write(Path file, List<String> lines) {
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
