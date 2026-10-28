package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.repository.PassRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores passes, one per line: {@code S|E;id;ticketId;price;consumed} (S = SpeedPass, E = Special Pass).
 */
public class TextPassRepository extends TextFileRepository implements PassRepository {

    public TextPassRepository(Path file) {
        super(file);
    }

    @Override
    public void save(Collection<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass pass : passes) {
            lines.add(join(pass instanceof SpecialPass ? "E" : "S", pass.getIdentifier(),
                    pass.getTicket().getIdentifier(), pass.getPrice().toPlainString(),
                    String.valueOf(pass.isConsumed())));
        }
        writeLines(lines);
    }

    @Override
    public List<Pass> load(Function<String, Optional<IndividualTicket>> tickets) {
        List<Pass> passes = new ArrayList<>();
        List<String> lines = readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            try {
                if (f.length != 5 || !(f[0].equals("S") || f[0].equals("E"))) {
                    throw new InvalidValueException("expected S|E;id;ticketId;price;consumed");
                }
                IndividualTicket ticket = tickets.apply(f[2])
                        .orElseThrow(() -> new InvalidValueException("unknown ticket " + f[2]));
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                passes.add(f[0].equals("S") ? new SpeedPass(f[1], ticket, price, consumed)
                        : new SpecialPass(f[1], ticket, price, consumed));
            } catch (InvalidValueException | NumberFormatException e) {
                problem(i + 1, line, e.getMessage());
            }
        }
        return passes;
    }
}
