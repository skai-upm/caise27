package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Reads and writes passes, one per line:
 * {@code SPEED|SPECIAL;identifier;ticketIdentifier;price;consumed}. Passes are
 * linked to the individual tickets already loaded.
 */
public class PassFileStorage {

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path file;
    private int skippedLines;

    public PassFileStorage(Path file) {
        this.file = file;
    }

    public List<Pass> load(TicketRepository tickets) {
        List<Pass> passes = new ArrayList<>();
        skippedLines = 0;
        for (String[] f : TextFiles.readRecords(file)) {
            Optional<Ticket> ticket = f.length == 5 ? tickets.findById(f[2]) : Optional.empty();
            if (ticket.isEmpty() || !(ticket.get() instanceof IndividualTicket individual)
                    || !(f[4].equals("true") || f[4].equals("false"))) {
                skippedLines++;
                continue;
            }
            try {
                BigDecimal price = new BigDecimal(f[3]);
                Pass pass;
                if (f[0].equals(SPEED)) {
                    pass = new SpeedPass(f[1], individual, price);
                } else if (f[0].equals(SPECIAL)) {
                    pass = new SpecialPass(f[1], individual, price);
                } else {
                    skippedLines++;
                    continue;
                }
                if (f[4].equals("true")) {
                    pass.consume();
                }
                passes.add(pass);
            } catch (NumberFormatException e) {
                skippedLines++;
            }
        }
        return passes;
    }

    public void save(Collection<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass pass : passes) {
            String type = pass instanceof SpecialPass ? SPECIAL : SPEED;
            lines.add(String.join(";", type, pass.getIdentifier(), pass.getTicket().getIdentifier(),
                    pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())));
        }
        TextFiles.writeLines(file, lines);
    }

    public int getSkippedLines() {
        return skippedLines;
    }
}
