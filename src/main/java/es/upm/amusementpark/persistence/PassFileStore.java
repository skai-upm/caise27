package es.upm.amusementpark.persistence;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.persistence.repository.TicketRepository;

/**
 * Stores passes as {@code SPEED|SPECIAL;id;ticketId;price;consumed}, linked on load to the
 * loaded individual tickets (direct or inside group tickets).
 */
public class PassFileStore {

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path file;

    public PassFileStore(Path file) {
        this.file = file;
    }

    public List<Pass> load(TicketRepository tickets) {
        List<Pass> passes = new ArrayList<>();
        for (String line : TextFiles.read(file)) {
            String[] f = TextFiles.fields(line);
            if (f.length != 5) {
                continue;
            }
            IndividualTicket ticket = tickets.findIndividualTicket(f[2]);
            if (ticket == null) {
                continue;
            }
            try {
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                if (f[0].equals(SPEED)) {
                    passes.add(new SpeedPass(f[1], ticket, price, consumed));
                } else if (f[0].equals(SPECIAL)) {
                    passes.add(new SpecialPass(f[1], ticket, price, consumed));
                }
            } catch (NumberFormatException e) {
                // malformed record: skipped
            }
        }
        return passes;
    }

    public void save(Collection<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(TextFiles.join(p instanceof SpecialPass ? SPECIAL : SPEED, p.getId(), p.getTicket().getId(),
                    p.getPrice().toPlainString(), String.valueOf(p.isConsumed())));
        }
        TextFiles.write(file, lines);
    }
}
