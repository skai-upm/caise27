package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.repository.PassRepository;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Stores passes as {@code SPEED|SPECIAL;id;ticketId;price;consumed}, in purchase order.
 */
public class TextPassRepository implements PassRepository {

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path file;

    public TextPassRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Pass> load(Function<String, IndividualTicket> ticketById) {
        List<Pass> passes = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            try {
                IndividualTicket ticket = f.length == 5 ? ticketById.apply(f[2]) : null;
                if (ticket == null || !(f[4].equals("true") || f[4].equals("false"))) {
                    throw new IllegalArgumentException();
                }
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                switch (f[0]) {
                    case SPEED -> passes.add(new SpeedPass(f[1], ticket, price, consumed));
                    case SPECIAL -> passes.add(new SpecialPass(f[1], ticket, price, consumed));
                    default -> throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {
                TextFiles.warnMalformed(file, line);
            }
        }
        return passes;
    }

    @Override
    public void save(List<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            String kind = p instanceof SpecialPass ? SPECIAL : SPEED;
            lines.add(String.join(TextFiles.SEPARATOR, kind, p.getId(), p.getTicket().getId(),
                    p.getPrice().toPlainString(), String.valueOf(p.isConsumed())));
        }
        TextFiles.writeLines(file, lines);
    }
}
