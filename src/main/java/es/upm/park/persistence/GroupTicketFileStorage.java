package es.upm.park.persistence;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.User;
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
 * Stores group tickets: {@code id;groupId;yyyy-MM-dd;individualPrice;total;dni1;dni2;...}.
 */
public class GroupTicketFileStorage {

    private static final String SEPARATOR = ";";
    private static final int FIXED_FIELDS = 5;

    private final Path file;

    public GroupTicketFileStorage(Path file) {
        this.file = file;
    }

    public List<GroupTicket> load(Function<String, Group> groupById, Function<String, User> userByDni) {
        List<GroupTicket> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                GroupTicket ticket = parse(line, groupById, userByDni);
                if (ticket != null) {
                    result.add(ticket);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read group tickets file " + file, e);
        }
        return result;
    }

    public void save(Collection<GroupTicket> tickets) {
        List<String> lines = new ArrayList<>();
        for (GroupTicket t : tickets) {
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
            throw new UncheckedIOException("Cannot write group tickets file " + file, e);
        }
    }

    static String format(GroupTicket t) {
        StringBuilder sb = new StringBuilder(String.join(SEPARATOR, t.getId(), t.getGroupId(),
                t.getDate().toString(), t.getIndividualPrice().toPlainString(), t.getPrice().toPlainString()));
        for (IndividualTicket c : t.getConstituents()) {
            sb.append(SEPARATOR).append(c.getOwnerDni());
        }
        return sb.toString();
    }

    private static GroupTicket parse(String line, Function<String, Group> groupById,
                                     Function<String, User> userByDni) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] f = line.split(SEPARATOR, -1);
        if (f.length <= FIXED_FIELDS || f[0].isBlank() || f[1].isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(f[2]);
            BigDecimal price = new BigDecimal(f[3]);
            BigDecimal total = new BigDecimal(f[4]);
            List<User> owners = new ArrayList<>();
            List<String> dnis = new ArrayList<>();
            for (int i = FIXED_FIELDS; i < f.length; i++) {
                dnis.add(f[i]);
                owners.add(userByDni.apply(f[i]));
            }
            return GroupTicket.of(f[0], groupById.apply(f[1]), f[1], date, price, total, owners, dnis);
        } catch (DateTimeParseException | NumberFormatException e) {
            return null;
        }
    }
}
