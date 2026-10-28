package es.upm.amusementpark.persistence.file;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;

/**
 * Stores passes as {@code S;id;ticketId;price;consumed} (SpeedPass) or
 * {@code P;id;ticketId;price;consumed} (Special Pass).
 */
public class PassFileStorage {

    private static final String SPEED = "S";
    private static final String SPECIAL = "P";

    private final Path file;

    public PassFileStorage(Path file) {
        this.file = file;
    }

    /**
     * Loads passes, resolving their individual ticket (standalone or in a group ticket).
     */
    public List<Pass> load(Function<String, Optional<IndividualTicket>> ticketById, List<String> warnings)
            throws IOException {
        List<Pass> passes = new ArrayList<>();
        int n = 0;
        for (String line : TextFiles.readLines(file)) {
            n++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            try {
                if (f.length != 5 || !(f[0].equals(SPEED) || f[0].equals(SPECIAL))) {
                    throw new IllegalArgumentException("malformed");
                }
                Optional<IndividualTicket> ticket = ticketById.apply(f[2]);
                if (ticket.isEmpty()) {
                    warnings.add("Skipping pass " + f[1] + " of unknown ticket " + f[2]);
                    continue;
                }
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                passes.add(f[0].equals(SPEED) ? new SpeedPass(f[1], ticket.get(), price, consumed)
                        : new SpecialPass(f[1], ticket.get(), price, consumed));
            } catch (IllegalArgumentException e) {
                warnings.add("Skipping malformed pass at line " + n + " of " + file);
            }
        }
        return passes;
    }

    public void save(List<Pass> passes) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(String.join(TextFiles.SEPARATOR, p instanceof SpeedPass ? SPEED : SPECIAL, p.getId(),
                    p.getTicket().getId(), p.getPrice().toPlainString(), String.valueOf(p.isConsumed())));
        }
        TextFiles.writeLines(file, lines);
    }
}
