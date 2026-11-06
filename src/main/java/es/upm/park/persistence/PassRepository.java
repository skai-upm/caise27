package es.upm.park.persistence;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Stores passes: {@code S|P;id;ticketId;price;consumed}. */
public class PassRepository {

    private static final String SPEED = "S";
    private static final String SPECIAL = "P";

    private final Path file;
    private final PrintStream warnings;

    public PassRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    /** Loads passes, resolving each ticket by identifier (composed tickets included). */
    public List<Pass> loadAll(Map<String, IndividualTicket> ticketsById) {
        return TextFile.parseAll(file, fields -> {
            TextFile.requireFields(fields, 5);
            IndividualTicket ticket = ticketsById.get(fields[2]);
            if (ticket == null) {
                throw new IllegalArgumentException("Unknown ticket " + fields[2]);
            }
            BigDecimal price = new BigDecimal(fields[3]);
            boolean consumed = Boolean.parseBoolean(fields[4]);
            return switch (fields[0]) {
                case SPEED -> new SpeedPass(fields[1], ticket, price, consumed);
                case SPECIAL -> new SpecialPass(fields[1], ticket, price, consumed);
                default -> throw new IllegalArgumentException("Unknown pass kind " + fields[0]);
            };
        }, warnings);
    }

    public void saveAll(List<Pass> passes) {
        TextFile.writeLines(file, passes.stream().map(pass -> String.join(TextFile.SEPARATOR,
                pass instanceof SpecialPass ? SPECIAL : SPEED, pass.getId(), pass.getTicket().getId(),
                pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed()))).toList());
    }
}
