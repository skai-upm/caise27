package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.PassType;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Stores passes in a text file, one per line: {@code TYPE;id;ticketId;price;consumed},
 * where TYPE is {@code SPEED} or {@code SPECIAL} and consumed is {@code true} or {@code false}.
 */
public class FilePassRepository implements PassRepository {

    public static final String DEFAULT_FILE_NAME = "passes.txt";

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 5;

    private final Path file;
    private final FileDataStore store;
    private final PrintStream warnings;

    public FilePassRepository(Path file, FileDataStore store, PrintStream warnings) {
        this.file = file;
        this.store = store;
        this.warnings = warnings;
    }

    /**
     * Loads all passes. Lines that are malformed, refer to an unknown
     * individual ticket, repeat an identifier or give a ticket a second
     * Special Pass are skipped with a warning.
     */
    @Override
    public List<Pass> loadAll(Function<String, IndividualTicket> ticketById) throws IOException {
        List<Pass> passes = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<IndividualTicket> withSpecialPass = new HashSet<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.trim().isEmpty()) {
                continue;
            }
            Pass pass = parse(line.split(SEPARATOR, -1), ticketById);
            boolean secondSpecial = pass instanceof SpecialPass && !withSpecialPass.add(pass.getTicket());
            if (pass == null || secondSpecial || !ids.add(pass.getId())) {
                warnings.println("WARNING: skipping line " + lineNumber + " of " + file
                        + ": malformed, duplicate or orphan pass");
            } else {
                passes.add(pass);
            }
        }
        return passes;
    }

    @Override
    public void saveAll(Collection<Pass> passes) throws IOException {
        store.write(file, toLines(passes));
    }

    static List<String> toLines(Collection<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass pass : passes) {
            lines.add(String.join(SEPARATOR, pass.getType().name(), pass.getId(), pass.getTicket().getId(),
                    pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())));
        }
        return lines;
    }

    private static Pass parse(String[] fields, Function<String, IndividualTicket> ticketById) {
        if (fields.length != FIELD_COUNT || fields[1].trim().isEmpty()) {
            return null;
        }
        IndividualTicket ticket = ticketById.apply(fields[2].trim());
        String consumed = fields[4].trim();
        if (ticket == null || !("true".equals(consumed) || "false".equals(consumed))) {
            return null;
        }
        try {
            BigDecimal price = new BigDecimal(fields[3].trim());
            if (price.signum() <= 0) {
                return null;
            }
            String id = fields[1].trim();
            boolean used = Boolean.parseBoolean(consumed);
            return switch (PassType.valueOf(fields[0].trim())) {
                case SPEED -> new SpeedPass(id, ticket, price, used);
                case SPECIAL -> new SpecialPass(id, ticket, price, used);
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
