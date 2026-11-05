package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.ParkException;
import es.upm.amusementpark.business.Pass;
import es.upm.amusementpark.business.SpecialPass;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists passes as {@code SPEED|SPECIAL;id;individualTicketId;consumed}. Prices are derived from the ticket.
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
                if (f.length != 4 || !(SPEED.equals(f[0]) || SPECIAL.equals(f[0]))
                        || !("true".equals(f[3]) || "false".equals(f[3]))) {
                    throw new IllegalArgumentException("unrecognised record");
                }
                park.restorePass(f[1], f[2], SPECIAL.equals(f[0]), Boolean.parseBoolean(f[3]));
            } catch (IllegalArgumentException | ParkException e) {
                warnings.add(file.getPath().getFileName() + " line " + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    public void save(ParkController park) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Pass pass : park.getPasses()) {
            lines.add(TextFile.join(pass instanceof SpecialPass ? SPECIAL : SPEED, pass.getId(),
                    pass.getTicket().getId(), String.valueOf(pass.isConsumed())));
        }
        file.writeLines(lines);
    }
}
