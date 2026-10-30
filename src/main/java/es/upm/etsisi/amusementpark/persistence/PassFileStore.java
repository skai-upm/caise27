package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores passes as {@code SPEED|SPECIAL;id;individualTicketId;price;consumed}, linked to tickets on load.
 */
public class PassFileStore {

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final LineFile file;
    private final TicketRepository tickets;

    public PassFileStore(Path file, TicketRepository tickets) {
        this.file = new LineFile(file);
        this.tickets = tickets;
    }

    public List<Pass> load() {
        List<Pass> passes = new ArrayList<>();
        for (String[] f : file.readRecords()) {
            try {
                passes.add(parse(f));
            } catch (RuntimeException e) {
                file.warnMalformed(f);
            }
        }
        return passes;
    }

    public void save(List<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(LineFile.join(p instanceof SpecialPass ? SPECIAL : SPEED,
                    p.getId(), p.getTicket().getId(), p.getPrice(), p.isConsumed()));
        }
        file.writeLines(lines);
    }

    private Pass parse(String[] f) {
        if (f.length != 5) {
            throw new IllegalArgumentException("Wrong number of fields");
        }
        IndividualTicket ticket = tickets.findById(f[2])
                .filter(IndividualTicket.class::isInstance)
                .map(IndividualTicket.class::cast)
                .orElseThrow(() -> new IllegalArgumentException("Unknown individual ticket " + f[2]));
        BigDecimal price = new BigDecimal(f[3]);
        boolean consumed = Boolean.parseBoolean(f[4]);
        return switch (f[0]) {
            case SPEED -> new SpeedPass(f[1], ticket, price, consumed);
            case SPECIAL -> new SpecialPass(f[1], ticket, price, consumed);
            default -> throw new IllegalArgumentException("Unknown pass type " + f[0]);
        };
    }
}
