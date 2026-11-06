package es.upm.park.persistence;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;

/**
 * Stores passes as {@code SPEED|SPECIAL;id;ticketId;price;consumed} lines.
 * Records whose ticket no longer exists are dropped when loading.
 */
public class PassFileStore {

    static final String SPEED = "SPEED";
    static final String SPECIAL = "SPECIAL";
    private static final int FIELDS = 5;

    private final Path file;

    public PassFileStore(Path file) {
        this.file = file;
    }

    public List<Pass> load(Function<String, Optional<IndividualTicket>> ticketById) {
        List<Pass> passes = new ArrayList<>();
        for (String line : TextFile.read(file)) {
            String[] f = TextFile.fields(line);
            if (f.length != FIELDS || !("true".equals(f[4]) || "false".equals(f[4]))) {
                continue;
            }
            Optional<IndividualTicket> ticket = ticketById.apply(f[2]);
            Optional<BigDecimal> price = price(f[3]);
            if (ticket.isEmpty() || price.isEmpty()) {
                continue;
            }
            boolean consumed = Boolean.parseBoolean(f[4]);
            if (SPEED.equals(f[0])) {
                passes.add(SpeedPass.restore(f[1], ticket.get(), price.get(), consumed));
            } else if (SPECIAL.equals(f[0])) {
                passes.add(SpecialPass.restore(f[1], ticket.get(), price.get(), consumed));
            }
        }
        return passes;
    }

    public void save(List<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(String.join(TextFile.SEPARATOR, p instanceof SpecialPass ? SPECIAL : SPEED, p.getId(),
                    p.getTicket().getId(), p.getPrice().toPlainString(), String.valueOf(p.isConsumed())));
        }
        TextFile.write(file, lines);
    }

    private static Optional<BigDecimal> price(String text) {
        try {
            return Optional.of(new BigDecimal(text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
