package es.upm.etsisi.amusementpark;

import java.io.IOException;
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
 * Stores group tickets in a text file, one per line:
 * {@code Id;yyyy-MM-dd;IdGrupo;PrecioIndividual;PrecioTotal;DNI1;DNI2;...}.
 * Constituent tickets are rebuilt from the member DNIs on load.
 */
public class GroupTicketFileRepository {

    private static final String SEPARATOR = ";";
    private static final int FIXED_FIELDS = 5;

    private final Path file;

    public GroupTicketFileRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all group tickets; a missing file means there are none.
     *
     * @param groupById returns the existing group with an id, or {@code null}
     * @param userByDni returns the existing user with a DNI, or {@code null}
     */
    public List<GroupTicket> load(Function<String, Group> groupById, Function<String, User> userByDni)
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
                if (f.length < FIXED_FIELDS) {
                    throw new IllegalArgumentException("wrong number of fields");
                }
                LocalDate date = LocalDate.parse(f[1]);
                BigDecimal base = new BigDecimal(f[3]);
                BigDecimal total = new BigDecimal(f[4]);
                List<IndividualTicket> parts = new ArrayList<>();
                for (int i = FIXED_FIELDS; i < f.length; i++) {
                    parts.add(new IndividualTicket(GroupTicket.partId(f[0], f[i]), date, f[i],
                            userByDni.apply(f[i]), base));
                }
                result.add(new GroupTicket(f[0], date, f[2], groupById.apply(f[2]), base, total, parts));
            } catch (IllegalArgumentException | DateTimeParseException e) {
                System.err.println("Skipping malformed group ticket record at line " + lineNumber + " of " + file);
            }
        }
        return result;
    }

    /** Replaces the file content with the given group tickets. */
    public void save(Collection<GroupTicket> groupTickets) throws IOException {
        List<String> lines = new ArrayList<>(groupTickets.size());
        for (GroupTicket t : groupTickets) {
            List<String> fields = new ArrayList<>();
            fields.add(t.getId());
            fields.add(t.getDate().toString());
            fields.add(t.getGroupId());
            fields.add(t.getBasePrice().toPlainString());
            fields.add(t.getPrice().toPlainString());
            for (IndividualTicket part : t.getParts()) {
                fields.add(part.getOwnerDni());
            }
            lines.add(String.join(SEPARATOR, fields));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
