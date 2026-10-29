package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.repository.PassRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Persists passes as {@code SPEED|SPECIAL;id;ticketId;price;consumed} lines. */
public class TextFilePassRepository extends TextFileRepository implements PassRepository {

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    public TextFilePassRepository(Path file) {
        super(file);
    }

    @Override
    public void saveAll(Collection<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass pass : passes) {
            lines.add(join(pass instanceof SpeedPass ? SPEED : SPECIAL, pass.getIdentifier(),
                    pass.getTicket().getIdentifier(), pass.getPrice(), pass.isConsumed()));
        }
        writeLines(lines);
    }

    @Override
    public void loadInto(ParkController park, List<String> skipped) {
        for (String line : readLines()) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            try {
                IndividualTicket ticket = f.length == 5 ? park.findIndividualTicket(f[2]) : null;
                if (ticket == null || !(f[0].equals(SPEED) || f[0].equals(SPECIAL))) {
                    skipped.add(line);
                    continue;
                }
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                park.restorePass(f[0].equals(SPEED) ? new SpeedPass(f[1], ticket, price, consumed)
                        : new SpecialPass(f[1], ticket, price, consumed));
            } catch (RuntimeException e) {
                skipped.add(line);
            }
        }
    }
}
