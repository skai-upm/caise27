package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Text-file pass storage: {@code SPEED|SPECIAL;passId;ticketId;price;consumed} per line.
 */
public class PassFileRepository {

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";
    private static final int FIELDS = 5;

    private final Path file;
    private final PrintStream warnings;

    public PassFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Pass> load(Map<String, IndividualTicket> ticketsById) throws IOException {
        List<Pass> passes = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            IndividualTicket ticket = f.length == FIELDS ? ticketsById.get(f[2]) : null;
            if (ticket == null || !(SPEED.equals(f[0]) || SPECIAL.equals(f[0]))) {
                warnings.println("WARNING: ignoring malformed pass line in " + file);
                continue;
            }
            BigDecimal price = new BigDecimal(f[3]);
            boolean consumed = Boolean.parseBoolean(f[4]);
            passes.add(SPEED.equals(f[0]) ? new SpeedPass(f[1], ticket, price, consumed)
                    : new SpecialPass(f[1], ticket, price, consumed));
        }
        return passes;
    }

    public void save(List<Pass> passes) throws IOException {
        TextFiles.writeLines(file, passes.stream().map(p -> String.join(TextFiles.SEPARATOR,
                p instanceof SpeedPass ? SPEED : SPECIAL, p.getId(), p.getTicket().getId(),
                p.getPrice().toPlainString(), Boolean.toString(p.isConsumed()))).toList());
    }
}
