package es.upm.park.persistence;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
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
 * Stores group tickets, one per line: {@code Id;yyyy-MM-dd;PrecioTotal;IdGrupo;PrecioIndividual;DNI...}.
 * Lines whose group or any member can no longer be resolved are kept verbatim as history.
 */
public class GroupTicketFileStore {

    private static final String SEPARATOR = ";";
    private static final int FIXED_FIELDS = 5;

    /** Content of the file: linked group tickets plus preserved historical lines. */
    public record Contents(List<GroupTicket> groupTickets, List<String> historicalLines) {
    }

    private final Path file;

    public GroupTicketFileStore(Path file) {
        this.file = file;
    }

    public Contents load(Function<String, Optional<Group>> groupById,
                         Function<String, Optional<User>> userByDni) throws IOException {
        List<GroupTicket> tickets = new ArrayList<>();
        List<String> history = new ArrayList<>();
        if (!Files.exists(file)) {
            return new Contents(tickets, history);
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            GroupTicket ticket = parse(line.split(SEPARATOR, -1), groupById, userByDni);
            if (ticket != null) {
                tickets.add(ticket);
            } else {
                history.add(line);
            }
        }
        return new Contents(tickets, history);
    }

    private static GroupTicket parse(String[] f, Function<String, Optional<Group>> groupById,
                                     Function<String, Optional<User>> userByDni) {
        if (f.length <= FIXED_FIELDS || f[0].isBlank()) {
            return null;
        }
        Optional<Group> group = groupById.apply(f[3]);
        if (group.isEmpty()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(f[1]);
            BigDecimal base = new BigDecimal(f[4]);
            List<IndividualTicket> constituents = new ArrayList<>();
            for (int i = FIXED_FIELDS; i < f.length; i++) {
                Optional<User> user = userByDni.apply(f[i]);
                if (user.isEmpty()) {
                    return null;
                }
                constituents.add(new IndividualTicket(GroupTicket.constituentId(f[0], user.get()), date, base, user.get()));
            }
            return new GroupTicket(f[0], date, group.get(), base, constituents);
        } catch (DateTimeParseException | NumberFormatException e) {
            return null;
        }
    }

    /** Identifiers used by a stored line: the group ticket id and each {@code id-DNI}. */
    public static List<String> idsOf(String line) {
        String[] f = line.split(SEPARATOR, -1);
        List<String> ids = new ArrayList<>();
        ids.add(f[0]);
        for (int i = FIXED_FIELDS; i < f.length; i++) {
            ids.add(f[0] + "-" + f[i]);
        }
        return ids;
    }

    public void save(List<GroupTicket> groupTickets, List<String> historicalLines) throws IOException {
        List<String> lines = new ArrayList<>();
        for (GroupTicket t : groupTickets) {
            StringBuilder line = new StringBuilder(String.join(SEPARATOR, t.getId(), t.getDate().toString(),
                    t.getPrice().toPlainString(), t.getGroup().getId(), t.getBasePrice().toPlainString()));
            for (IndividualTicket member : t.getTickets()) {
                line.append(SEPARATOR).append(member.getUser().getDni());
            }
            lines.add(line.toString());
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
