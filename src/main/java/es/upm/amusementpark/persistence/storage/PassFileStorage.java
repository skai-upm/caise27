package es.upm.amusementpark.persistence.storage;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores passes in a text file, one per line: kind(S|P);identifier;ticketIdentifier;price;consumed(1|0).
 */
public class PassFileStorage implements PassStorage {

    private final Path file;

    public PassFileStorage(Path file) {
        this.file = file;
    }

    @Override
    public List<Pass> load(TicketRepository tickets) {
        List<Pass> passes = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            if (f.length != 5) {
                continue;
            }
            IndividualTicket ticket = tickets.findById(f[2]).filter(IndividualTicket.class::isInstance)
                    .map(IndividualTicket.class::cast).orElse(null);
            if (ticket == null) {
                continue;
            }
            try {
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = "1".equals(f[4]);
                if ("S".equals(f[0])) {
                    passes.add(new SpeedPass(f[1], ticket, price, consumed));
                } else if ("P".equals(f[0])) {
                    passes.add(new SpecialPass(f[1], ticket, price, consumed));
                }
            } catch (NumberFormatException e) {
                // malformed line: ignored
            }
        }
        return passes;
    }

    @Override
    public void save(Collection<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(String.join(TextFiles.SEPARATOR, p instanceof SpecialPass ? "P" : "S", p.getIdentifier(),
                    p.getTicket().getIdentifier(), p.getPrice().toPlainString(), p.isConsumed() ? "1" : "0"));
        }
        TextFiles.writeLines(file, lines);
    }
}
