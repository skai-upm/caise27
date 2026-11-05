package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.GroupTicket;
import es.upm.amusementpark.business.IndividualTicket;
import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.ParkException;
import es.upm.amusementpark.business.Ticket;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists tickets:
 * {@code INDIVIDUAL;id;date;price;email},
 * {@code GROUP;id;date;groupId;linked;componentCount} followed by
 * {@code COMPONENT;id;date;price;email} lines.
 */
public class TicketRepository {

    private static final String INDIVIDUAL = "INDIVIDUAL";
    private static final String GROUP = "GROUP";
    private static final String COMPONENT = "COMPONENT";

    private final TextFile file;

    public TicketRepository(Path path) {
        this.file = new TextFile(path);
    }

    public void load(ParkController park, List<String> warnings) throws IOException {
        List<String> lines = file.readLines();
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            int lineNumber = i + 1;
            i++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFile.fields(line);
            try {
                if (INDIVIDUAL.equals(f[0]) && f.length == 5) {
                    park.restoreIndividualTicket(f[1], f[4], LocalDate.parse(f[2]), new BigDecimal(f[3]));
                } else if (GROUP.equals(f[0]) && f.length == 6) {
                    int count = Integer.parseInt(f[5]);
                    if (count < 1 || i + count > lines.size()) {
                        throw new IllegalArgumentException("invalid component count");
                    }
                    List<IndividualTicket> components = new ArrayList<>();
                    for (int c = 0; c < count; c++) {
                        components.add(component(lines.get(i + c)));
                    }
                    i += count;
                    park.restoreGroupTicket(f[1], f[3], Boolean.parseBoolean(f[4]), LocalDate.parse(f[2]),
                            components);
                } else {
                    throw new IllegalArgumentException("unrecognised record");
                }
            } catch (RuntimeException e) {
                warnings.add(file.getPath().getFileName() + " line " + lineNumber + ": " + e.getMessage());
            }
        }
    }

    private static IndividualTicket component(String line) {
        String[] f = TextFile.fields(line);
        if (f.length != 5 || !COMPONENT.equals(f[0])) {
            throw new IllegalArgumentException("invalid component record");
        }
        return new IndividualTicket(f[1], f[4], LocalDate.parse(f[2]), new BigDecimal(f[3]));
    }

    public void save(ParkController park) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : park.getTickets()) {
            if (ticket instanceof GroupTicket group) {
                lines.add(TextFile.join(GROUP, group.getId(), group.getDate().toString(), group.getGroupId(),
                        String.valueOf(group.getGroup() != null), String.valueOf(group.getComponents().size())));
                for (IndividualTicket c : group.getComponents()) {
                    lines.add(TextFile.join(COMPONENT, c.getId(), c.getDate().toString(),
                            c.getPrice().toPlainString(), c.getUserEmail()));
                }
            } else if (ticket instanceof IndividualTicket single && single.getContainingGroupTicket() == null) {
                lines.add(TextFile.join(INDIVIDUAL, single.getId(), single.getDate().toString(),
                        single.getPrice().toPlainString(), single.getUserEmail()));
            }
        }
        file.writeLines(lines);
    }
}
