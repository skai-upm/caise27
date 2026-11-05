package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.ParkException;
import es.upm.amusementpark.business.Pass;
import es.upm.amusementpark.business.SpecialPass;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists passes as {@code SPEED|SPECIAL;id;individualTicketId;price;consumed}. Lines without a price (older
 * format) derive it from the ticket.
 */
public class PassRepository {

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final TextFile file;

    public PassRepository(Path path) {
        this.file = new TextFile(path);
    }

    public void load(ParkController park, List<String> warnings) throws IOException {
        List<String> lines = file.readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFile.fields(line);
            try {
                String consumed = f[f.length - 1];
                if ((f.length != 4 && f.length != 5) || !(SPEED.equals(f[0]) || SPECIAL.equals(f[0]))
                        || !("true".equals(consumed) || "false".equals(consumed))) {
                    throw new IllegalArgumentException("unrecognised record");
                }
                BigDecimal price = f.length == 5 ? new BigDecimal(f[3]) : null;
                park.restorePass(f[1], f[2], SPECIAL.equals(f[0]), price, Boolean.parseBoolean(consumed));
            } catch (IllegalArgumentException | ParkException e) {
                warnings.add(file.getPath().getFileName() + " line " + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    public void save(ParkController park) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Pass pass : park.getPasses()) {
            lines.add(TextFile.join(pass instanceof SpecialPass ? SPECIAL : SPEED, pass.getId(),
                    pass.getTicket().getId(), pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())));
        }
        file.writeLines(lines);
    }
}
