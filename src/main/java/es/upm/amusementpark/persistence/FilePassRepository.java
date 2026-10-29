package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.pass.SpecialPass;
import es.upm.amusementpark.business.model.pass.SpeedPass;
import es.upm.amusementpark.business.model.ticket.IndividualTicket;
import es.upm.amusementpark.business.model.ticket.Ticket;
import es.upm.amusementpark.business.repository.PassRepository;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stores passes as {@code SPEED|SPECIAL;id;ticketId;price;consumed}. Passes whose individual ticket is
 * unknown are skipped with a warning.
 */
public class FilePassRepository implements PassRepository {

    private static final String SEPARATOR = ";";
    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";
    private static final int FIELDS = 5;

    private final Path path;
    private final PrintStream warnings;

    public FilePassRepository(Path path, PrintStream warnings) {
        this.path = path;
        this.warnings = warnings;
    }

    @Override
    public List<Pass> load(Map<String, Ticket> ticketsById) throws IOException {
        List<Pass> passes = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(path)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            boolean wellFormed = f.length == FIELDS && (f[0].equals(SPEED) || f[0].equals(SPECIAL))
                    && (f[4].equals("true") || f[4].equals("false"));
            if (!wellFormed || !(ticketsById.get(f[2]) instanceof IndividualTicket ticket)) {
                warnings.println("WARNING: skipping malformed or orphan pass at line " + lineNumber + " in " + path);
                continue;
            }
            try {
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                passes.add(f[0].equals(SPEED) ? new SpeedPass(f[1], ticket, price, consumed)
                        : new SpecialPass(f[1], ticket, price, consumed));
            } catch (NumberFormatException e) {
                warnings.println("WARNING: skipping malformed pass at line " + lineNumber + " in " + path);
            }
        }
        return passes;
    }

    @Override
    public void saveAll(Collection<Pass> passes) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Pass pass : passes) {
                writer.write(String.join(SEPARATOR, pass instanceof SpeedPass ? SPEED : SPECIAL, pass.getId(),
                        pass.getTicket().getId(), pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())));
                writer.newLine();
            }
        }
    }
}
