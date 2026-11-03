package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.IndividualTicket;
import es.upm.etsisi.park.business.Pass;
import es.upm.etsisi.park.business.SpecialPass;
import es.upm.etsisi.park.business.SpeedPass;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores passes as {@code S;id;ticketId;price;consumed} (SpeedPass) or
 * {@code P;id;ticketId;price;consumed} (Special Pass).
 */
public class PassRepository extends TextFileRepository {

    private static final String SPEED = "S";
    private static final String SPECIAL = "P";
    private static final int FIELDS = 5;

    public PassRepository(Path file) {
        super(file);
    }

    public List<Pass> load(Function<String, IndividualTicket> ticketById) throws IOException {
        List<Pass> passes = new ArrayList<>();
        for (String line : readLines()) {
            Pass pass = parse(line, ticketById);
            if (pass == null) {
                warnMalformed(line);
            } else {
                passes.add(pass);
            }
        }
        return passes;
    }

    public void saveAll(Collection<Pass> passes) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            String type = p instanceof SpeedPass ? SPEED : SPECIAL;
            lines.add(String.join(SEPARATOR, type, p.getId(), p.getTicket().getId(), p.getPrice().toPlainString(),
                    String.valueOf(p.isConsumed())));
        }
        writeLines(lines);
    }

    /** Builds the pass described by the record, or returns {@code null} if the record is invalid. */
    private static Pass parse(String line, Function<String, IndividualTicket> ticketById) {
        String[] f = fields(line, FIELDS);
        if (f.length == 0 || !isBoolean(f[4])) {
            return null;
        }
        IndividualTicket ticket = ticketById.apply(f[2]);
        if (ticket == null) {
            return null;
        }
        BigDecimal price;
        try {
            price = new BigDecimal(f[3]);
        } catch (NumberFormatException e) {
            return null;
        }
        boolean consumed = Boolean.parseBoolean(f[4]);
        return switch (f[0]) {
            case SPEED -> new SpeedPass(f[1], ticket, price, consumed);
            case SPECIAL -> new SpecialPass(f[1], ticket, price, consumed);
            default -> null;
        };
    }

    private static boolean isBoolean(String text) {
        return "true".equals(text) || "false".equals(text);
    }
}
