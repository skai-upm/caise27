package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.IndividualTicket;
import es.upm.etsisi.park.business.Pass;
import es.upm.etsisi.park.business.SpecialPass;
import es.upm.etsisi.park.business.SpeedPass;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Stores passes as {@code S|P;id;ticketId;price;consumed} (S = SpeedPass, P = Special Pass).
 */
class PassFileStore {

    private static final String SPEED = "S";
    private static final String SPECIAL = "P";
    private static final int FIELDS = 5;

    private final Path file;

    PassFileStore(Path file) {
        this.file = file;
    }

    Path getFile() {
        return file;
    }

    List<Pass> load(Map<String, IndividualTicket> ticketsById) {
        List<Pass> passes = new ArrayList<>();
        for (String line : TextFile.readLines(file)) {
            String[] f = TextFile.fields(line);
            IndividualTicket ticket = f.length == FIELDS ? ticketsById.get(f[2]) : null;
            if (ticket == null) {
                TextFile.warnMalformed(file, line);
                continue;
            }
            BigDecimal price = new BigDecimal(f[3]);
            boolean consumed = Boolean.parseBoolean(f[4]);
            if (SPEED.equals(f[0])) {
                passes.add(new SpeedPass(f[1], ticket, price, consumed));
            } else if (SPECIAL.equals(f[0])) {
                passes.add(new SpecialPass(f[1], ticket, price, consumed));
            } else {
                TextFile.warnMalformed(file, line);
            }
        }
        return passes;
    }

    List<String> toLines(Collection<Pass> passes) {
        return passes.stream()
                .map(p -> TextFile.join(p instanceof SpeedPass ? SPEED : SPECIAL, p.getId(), p.getTicket().getId(),
                        p.getPrice().toPlainString(), Boolean.toString(p.isConsumed())))
                .toList();
    }
}
