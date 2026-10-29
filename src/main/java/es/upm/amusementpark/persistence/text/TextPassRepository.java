package es.upm.amusementpark.persistence.text;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.persistence.PassRepository;

/**
 * Stores passes as {@code SPEED|SPECIAL;passId;ticketId;price;consumed}.
 */
public class TextPassRepository implements PassRepository {

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path file;

    public TextPassRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Pass> load(Map<String, IndividualTicket> ticketsById) {
        List<Pass> passes = new ArrayList<>();
        for (String[] f : TextFiles.readRecords(file)) {
            IndividualTicket ticket = f.length == 5 ? ticketsById.get(f[2]) : null;
            if (ticket == null) {
                TextFiles.warnMalformed(file);
                continue;
            }
            try {
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                switch (f[0]) {
                    case SPEED -> passes.add(new SpeedPass(f[1], price, ticket, consumed));
                    case SPECIAL -> passes.add(new SpecialPass(f[1], price, ticket, consumed));
                    default -> TextFiles.warnMalformed(file);
                }
            } catch (NumberFormatException e) {
                TextFiles.warnMalformed(file);
            }
        }
        return passes;
    }

    @Override
    public void save(Collection<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            String kind = p instanceof SpecialPass ? SPECIAL : SPEED;
            lines.add(TextFiles.join(kind, p.getId(), p.getTicket().getId(), p.getPrice().toPlainString(),
                    String.valueOf(p.isConsumed())));
        }
        TextFiles.writeLines(file, lines);
    }
}
