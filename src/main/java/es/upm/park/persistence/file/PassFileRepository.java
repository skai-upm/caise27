package es.upm.park.persistence.file;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.pass.Pass;
import es.upm.park.business.model.pass.SpecialPass;
import es.upm.park.business.model.pass.SpeedPass;
import es.upm.park.persistence.PassRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Stores passes as {@code SPEED|SPECIAL;id;ticketId;price;consumed}.
 */
public class PassFileRepository implements PassRepository {

    private static final String SPEED_KIND = "SPEED";
    private static final String SPECIAL_KIND = "SPECIAL";
    private static final int FIELD_COUNT = 5;

    private final Path file;

    public PassFileRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Pass> loadAll(Function<String, IndividualTicket> ticketById) {
        List<Pass> passes = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            Pass pass = parse(TextFiles.split(line), ticketById);
            if (pass == null) {
                TextFiles.warnMalformed(file, line);
            } else {
                passes.add(pass);
            }
        }
        return passes;
    }

    @Override
    public void saveAll(Iterable<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass pass : passes) {
            String kind = pass instanceof SpecialPass ? SPECIAL_KIND : SPEED_KIND;
            lines.add(String.join(TextFiles.SEPARATOR, kind, pass.getId(), pass.getTicket().getId(),
                    pass.getPrice().toPlainString(), Boolean.toString(pass.isConsumed())));
        }
        TextFiles.writeLines(file, lines);
    }

    private static Pass parse(String[] f, Function<String, IndividualTicket> ticketById) {
        if (f.length != FIELD_COUNT || f[1].isEmpty()) {
            return null;
        }
        IndividualTicket ticket = ticketById.apply(f[2]);
        if (ticket == null || !("true".equals(f[4]) || "false".equals(f[4]))) {
            return null;
        }
        try {
            BigDecimal price = new BigDecimal(f[3]);
            boolean consumed = Boolean.parseBoolean(f[4]);
            return switch (f[0]) {
                case SPEED_KIND -> new SpeedPass(f[1], ticket, price, consumed);
                case SPECIAL_KIND -> new SpecialPass(f[1], ticket, price, consumed);
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
