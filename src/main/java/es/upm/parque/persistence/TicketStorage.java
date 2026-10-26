package es.upm.parque.persistence;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.PassStatus;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Stores tickets in three text files:
 * individual tickets as {@code Id;yyyy-MM-dd;Precio;DNI}, group tickets as
 * {@code Id;yyyy-MM-dd;IdGrupo;PrecioBase;Descuento;Id-1:DNI,Id-2:DNI,...} and personal passes as
 * {@code SPEED|SPECIAL;IdPase;IdTicket;Precio;Estado}.
 */
public class TicketStorage implements TicketRepository {

    private static final String SEPARATOR = ";";
    private static final String COMPONENT_SEPARATOR = ",";
    private static final String OWNER_SEPARATOR = ":";
    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path ticketsFile;
    private final Path groupTicketsFile;
    private final Path passesFile;

    public TicketStorage(Path ticketsFile, Path groupTicketsFile, Path passesFile) {
        this.ticketsFile = ticketsFile;
        this.groupTicketsFile = groupTicketsFile;
        this.passesFile = passesFile;
    }

    /**
     * @param userByDni resolves a DNI to the registered user, or null if it no longer exists
     * @param groupById resolves a group identifier to the group, or null if it no longer exists
     */
    @Override
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
        loadPasses(tickets);
        return tickets;
    }

    private void loadPasses(List<Ticket> tickets) {
        Map<String, IndividualTicket> byId = new HashMap<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it) {
                byId.put(it.getId(), it);
            } else if (t instanceof GroupTicket gt) {
                gt.getComponents().forEach(c -> byId.put(c.getId(), c));
            }
        }
        for (String line : readLines(passesFile)) {
            String[] f = line.split(SEPARATOR, -1);
            IndividualTicket ticket = f.length == 5 ? byId.get(f[2]) : null;
            if (ticket != null) {
                try {
                    BigDecimal price = new BigDecimal(f[3]);
                    PassStatus status = PassStatus.valueOf(f[4]);
                    if (f[0].equals(SPEED)) {
                        ticket.restorePass(new SpeedPass(f[1], price, status));
                    } else if (f[0].equals(SPECIAL)) {
                        ticket.restorePass(new SpecialPass(f[1], price, status));
                    }
                } catch (IllegalArgumentException e) {
                    // malformed line: skipped
                }
            }
        }
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

    @Override
    public void save(Collection<Ticket> tickets) {
        List<String> individual = new ArrayList<>();
        List<String> group = new ArrayList<>();
        List<String> passes = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t instanceof IndividualTicket it) {
                individual.add(String.join(SEPARATOR, it.getId(), it.getDate().toString(),
                    it.getPrice().toPlainString(), it.getOwnerDni()));
                addPassLines(it, passes);
            } else if (t instanceof GroupTicket gt) {
                List<String> components = new ArrayList<>();
                for (IndividualTicket c : gt.getComponents()) {
                    components.add(c.getId() + OWNER_SEPARATOR + c.getOwnerDni());
                    addPassLines(c, passes);
                }
                group.add(String.join(SEPARATOR, gt.getId(), gt.getDate().toString(), gt.getGroupId(),
                    gt.getBasePrice().toPlainString(), String.valueOf(gt.getDiscountPercent()),
                    String.join(COMPONENT_SEPARATOR, components)));
            }
        }
        write(ticketsFile, individual);
        write(groupTicketsFile, group);
        write(passesFile, passes);
    }

    private static void addPassLines(IndividualTicket ticket, List<String> lines) {
        for (Pass p : ticket.getPasses()) {
            lines.add(String.join(SEPARATOR, p instanceof SpecialPass ? SPECIAL : SPEED, p.getId(), ticket.getId(),
                p.getPrice().toPlainString(), p.getStatus().name()));
        }
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
