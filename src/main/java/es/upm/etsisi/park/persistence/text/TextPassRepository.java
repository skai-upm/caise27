package es.upm.etsisi.park.persistence.text;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.business.model.ticket.IndividualTicket;
import es.upm.etsisi.park.persistence.PassRepository;

/**
 * Stores passes one per line: {@code SPEED|SPECIAL;id;ticketId;price;consumed}.
 * Loaded passes are returned with their ticket reference set; attaching
 * them to the ticket is left to the caller.
 */
public class TextPassRepository implements PassRepository {

    public static final String FILE_NAME = "passes.txt";

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
    public List<Pass> load(Map<String, IndividualTicket> individualTicketsById) throws IOException {
        List<Pass> passes = new ArrayList<>();
        List<String> lines = TextFiles.readLines(file);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            try {
                if (f.length != FIELD_COUNT) {
                    throw new IllegalArgumentException("unexpected field count");
                }
                IndividualTicket ticket = individualTicketsById.get(f[2]);
                if (ticket == null) {
                    throw new IllegalArgumentException("unknown ticket " + f[2]);
                }
                boolean consumed = parseBoolean(f[4]);
                BigDecimal price = new BigDecimal(f[3]);
                passes.add(switch (f[0]) {
                    case SPEED -> new SpeedPass(f[1], ticket, price, consumed);
                    case SPECIAL -> new SpecialPass(f[1], ticket, price, consumed);
                    default -> throw new IllegalArgumentException("unknown pass type " + f[0]);
                });
            } catch (IllegalArgumentException e) {
                warnings.println("WARNING: skipping malformed line " + (i + 1) + " in " + file + ": " + e.getMessage());
            }
        }
        return passes;
    }

    @Override
    public void save(List<Pass> passes) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Pass pass : passes) {
            lines.add(String.join(TextFiles.SEPARATOR, pass instanceof SpecialPass ? SPECIAL : SPEED, pass.getId(),
                    pass.getTicket().getId(), pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())));
        }
        TextFiles.write(file, lines);
    }

    private static boolean parseBoolean(String text) {
        if ("true".equals(text)) {
            return true;
        }
        if ("false".equals(text)) {
            return false;
        }
        throw new IllegalArgumentException("invalid consumed flag " + text);
    }
}
