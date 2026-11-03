package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.Values;
import es.upm.etsisi.amusementpark.business.repository.PassRepository;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Pass;
import es.upm.etsisi.amusementpark.business.ticket.SpecialPass;
import es.upm.etsisi.amusementpark.business.ticket.SpeedPass;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores passes in a text file, one per line:
 * {@code SPEED|SPECIAL;Identifier;TicketIdentifier;Price;Consumed}.
 */
public class TextPassRepository implements PassRepository {

    private static final String SEPARATOR = ";";
    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";
    private static final int FIELD_COUNT = 5;

    private final Path file;
    private final PrintStream warnings;

    public TextPassRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<Pass> load(Map<String, IndividualTicket> ticketsById) throws StorageException {
        List<Pass> passes = new ArrayList<>();
        Set<String> identifiers = new HashSet<>();
        Set<IndividualTicket> withSpecialPass = new HashSet<>();
        int lineNumber = 0;
        for (String line : TextFile.readLines(file)) {
            lineNumber++;
            if (!line.isBlank()) {
                Pass pass = parse(line.split(SEPARATOR, -1), ticketsById);
                boolean duplicateSpecial = pass instanceof SpecialPass && !withSpecialPass.add(pass.getTicket());
                if (pass != null && !duplicateSpecial && identifiers.add(pass.getIdentifier())) {
                    passes.add(pass);
                } else {
                    warnings.println("WARNING: skipping invalid pass record at line " + lineNumber + " of " + file);
                }
            }
        }
        return passes;
    }

    private static Pass parse(String[] fields, Map<String, IndividualTicket> ticketsById) {
        if (fields.length != FIELD_COUNT || fields[1].isBlank()) {
            return null;
        }
        IndividualTicket ticket = ticketsById.get(fields[2]);
        BigDecimal price = Values.parsePrice(fields[3]);
        boolean validState = "true".equals(fields[4]) || "false".equals(fields[4]);
        if (ticket == null || price == null || !validState) {
            return null;
        }
        boolean consumed = Boolean.parseBoolean(fields[4]);
        return switch (fields[0]) {
            case SPEED -> new SpeedPass(fields[1], ticket, price, consumed);
            case SPECIAL -> new SpecialPass(fields[1], ticket, price, consumed);
            default -> null;
        };
    }

    @Override
    public void save(List<Pass> passes) throws StorageException {
        List<String> lines = new ArrayList<>();
        for (Pass pass : passes) {
            String kind = pass instanceof SpecialPass ? SPECIAL : SPEED;
            lines.add(String.join(SEPARATOR, kind, pass.getIdentifier(), pass.getTicket().getIdentifier(),
                    pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())));
        }
        TextFile.writeLines(file, lines);
    }
}
