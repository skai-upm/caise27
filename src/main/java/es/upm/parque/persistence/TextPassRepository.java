package es.upm.parque.persistence;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.PassKind;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.repository.PassRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores passes as lines {@code id;SPEED|SPECIAL;ticketId;price;consumed}.
 * Passes whose ticket no longer exists are skipped with a warning.
 */
public class TextPassRepository implements PassRepository {

    public static final String FILE_NAME = "passes.txt";

    private final Path file;

    public TextPassRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Pass> load(Function<String, IndividualTicket> ticketsById) {
        List<Pass> passes = new ArrayList<>();
        for (String[] fields : TextStore.readRecords(file)) {
            try {
                IndividualTicket ticket = fields.length == 5 ? ticketsById.apply(fields[2]) : null;
                if (ticket == null || !(fields[4].equals("true") || fields[4].equals("false"))) {
                    throw new IllegalArgumentException();
                }
                BigDecimal price = new BigDecimal(fields[3]);
                boolean consumed = Boolean.parseBoolean(fields[4]);
                passes.add(switch (PassKind.valueOf(fields[1])) {
                    case SPEED -> new SpeedPass(fields[0], ticket, price, consumed);
                    case SPECIAL -> new SpecialPass(fields[0], ticket, price, consumed);
                });
            } catch (IllegalArgumentException e) {
                TextStore.warnMalformed(file, fields);
            }
        }
        return passes;
    }

    @Override
    public void saveAll(Collection<Pass> passes) {
        TextStore.writeLines(file, passes.stream()
                .map(pass -> String.join(TextStore.SEPARATOR, pass.getId(), pass.getKind().name(),
                        pass.getTicket().getId(), pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())))
                .toList());
    }
}
