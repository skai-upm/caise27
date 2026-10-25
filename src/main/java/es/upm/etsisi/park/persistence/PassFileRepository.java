package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Pass;
import es.upm.etsisi.park.business.model.PassType;
import es.upm.etsisi.park.business.model.SpecialPass;
import es.upm.etsisi.park.business.model.SpeedPass;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores passes as {@code SPEED|SPECIAL;id;individualTicketId;price;consumed}.
 */
public class PassFileRepository implements PassRepository {

    private static final int FIELD_COUNT = 5;

    private final Path file;
    private final PrintStream warnings;

    public PassFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<Pass> loadAll(Function<String, Optional<IndividualTicket>> ticketById) {
        List<Pass> passes = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] parts = TextFiles.fields(line);
            try {
                if (parts.length != FIELD_COUNT) {
                    throw new IllegalArgumentException("expected " + FIELD_COUNT + " fields");
                }
                IndividualTicket ticket = ticketById.apply(parts[2])
                        .orElseThrow(() -> new IllegalArgumentException("unknown ticket " + parts[2]));
                BigDecimal price = new BigDecimal(parts[3]);
                boolean consumed = Boolean.parseBoolean(parts[4]);
                passes.add(switch (PassType.valueOf(parts[0])) {
                    case SPEED -> new SpeedPass(parts[1], price, ticket, consumed);
                    case SPECIAL -> new SpecialPass(parts[1], price, ticket, consumed);
                });
            } catch (RuntimeException e) {
                warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + file + ": "
                        + e.getMessage());
            }
        }
        return passes;
    }

    @Override
    public void saveAll(Collection<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass pass : passes) {
            lines.add(String.join(TextFiles.SEPARATOR, pass.getType().name(), pass.getId(), pass.getTicket().getId(),
                    pass.getPrice().toPlainString(), Boolean.toString(pass.isConsumed())));
        }
        TextFiles.writeLines(file, lines);
    }
}
