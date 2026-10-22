package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Ticket;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores passes as {@code SPEED|SPECIAL;id;ticketId;price;consumed}. Passes whose ticket
 * no longer exists are skipped.
 */
public class PassFileStore {

    private static final String SEPARATOR = ";";
    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";
    private static final int FIELD_COUNT = 5;

    private final Path file;

    public PassFileStore(Path file) {
        this.file = file;
    }

    public List<Pass> load(Function<String, Optional<Ticket>> ticketById) {
        List<Pass> passes = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = line.split(SEPARATOR, -1);
            Optional<IndividualTicket> ticket = f.length == FIELD_COUNT
                    ? ticketById.apply(f[2]).filter(IndividualTicket.class::isInstance).map(IndividualTicket.class::cast)
                    : Optional.empty();
            try {
                if (ticket.isEmpty()) {
                    throw new IllegalArgumentException("unknown ticket");
                }
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                if (SPEED.equals(f[0])) {
                    passes.add(new SpeedPass(f[1], ticket.get(), price, consumed));
                } else if (SPECIAL.equals(f[0])) {
                    passes.add(new SpecialPass(f[1], ticket.get(), price, consumed));
                } else {
                    throw new IllegalArgumentException("unknown pass kind");
                }
            } catch (IllegalArgumentException e) {
                TextFiles.warnMalformed(file, line);
            }
        }
        return passes;
    }

    public void save(List<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass pass : passes) {
            String kind = pass instanceof SpecialPass ? SPECIAL : SPEED;
            lines.add(String.join(SEPARATOR, kind, pass.getId(), pass.getTicket().getId(),
                    pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())));
        }
        TextFiles.writeLines(file, lines);
    }
}
