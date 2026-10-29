package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.TicketRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Persists tickets. Individual tickets: {@code INDIVIDUAL;id;date;price;email;dni}.
 * Group tickets: {@code GROUP;id;date;groupId} ({@code -} when the group no
 * longer exists) followed by {@code COMPONENT;groupTicketId;id;price;email;dni}
 * lines. User data is embedded so historical tickets can always be rebuilt.
 */
public class TextFileTicketRepository extends TextFileRepository
        implements TicketRepository {

    private static final String NO_GROUP = "-";

    public TextFileTicketRepository(Path file) {
        super(file);
    }

    @Override
    public void saveAll(Collection<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket) {
                Group group = groupTicket.getGroup();
                lines.add(join("GROUP", ticket.getIdentifier(), ticket.getDate(),
                        group == null ? NO_GROUP : group.getIdentifier()));
                for (IndividualTicket component : groupTicket.getComposition()) {
                    lines.add(join("COMPONENT", ticket.getIdentifier(), component.getIdentifier(),
                            component.getPrice(), component.getUser().getEmail(), component.getUser().getDni()));
                }
            } else if (ticket instanceof IndividualTicket individual) {
                lines.add(join("INDIVIDUAL", ticket.getIdentifier(), ticket.getDate(), ticket.getPrice(),
                        individual.getUser().getEmail(), individual.getUser().getDni()));
            }
        }
        writeLines(lines);
    }

    @Override
    public void loadInto(ParkController park, List<String> skipped) {
        List<String> pending = null;
        List<String> pendingLines = new ArrayList<>();
        for (String line : readLines()) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            if (f[0].equals("COMPONENT") && pending != null && f.length == 6 && f[1].equals(pending.get(0))) {
                pendingLines.add(line);
                continue;
            }
            if (pending != null) {
                restoreGroupTicket(park, pending, pendingLines, skipped);
                pending = null;
                pendingLines = new ArrayList<>();
            }
            if (f[0].equals("GROUP") && f.length == 4) {
                pending = List.of(f[1], f[2], f[3], line);
            } else if (f[0].equals("INDIVIDUAL") && f.length == 6) {
                try {
                    User user = park.resolveUser(f[4], f[5]);
                    park.restoreTicket(new IndividualTicket(f[1], user, LocalDate.parse(f[2]),
                            new BigDecimal(f[3])));
                } catch (RuntimeException e) {
                    skipped.add(line);
                }
            } else {
                skipped.add(line);
            }
        }
        if (pending != null) {
            restoreGroupTicket(park, pending, pendingLines, skipped);
        }
    }

    private static void restoreGroupTicket(ParkController park, List<String> header,
            List<String> componentLines, List<String> skipped) {
        try {
            LocalDate date = LocalDate.parse(header.get(1));
            List<IndividualTicket> composition = new ArrayList<>();
            for (String componentLine : componentLines) {
                String[] c = fields(componentLine);
                composition.add(new IndividualTicket(c[2], park.resolveUser(c[4], c[5]), date,
                        new BigDecimal(c[3])));
            }
            Group group = header.get(2).equals(NO_GROUP) ? null : park.findGroup(header.get(2));
            park.restoreTicket(new GroupTicket(header.get(0), group, date, composition));
        } catch (RuntimeException e) {
            skipped.add(header.get(3));
            skipped.addAll(componentLines);
        }
    }
}
