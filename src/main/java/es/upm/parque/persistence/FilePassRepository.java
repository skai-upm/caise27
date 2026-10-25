package es.upm.parque.persistence;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.repository.PassRepository;

/**
 * Stores passes as {@code id;SPEED|SPECIAL;ticketId;price;consumed}.
 */
public class FilePassRepository implements PassRepository {

    private static final Logger LOGGER = Logger.getLogger(FilePassRepository.class.getName());

    private static final int FIELDS = 5;
    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path file;

    public FilePassRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Pass> load(Function<String, IndividualTicket> ticketById) {
        List<Pass> passes = new ArrayList<>();
        for (String[] f : TextFile.readRecords(file, FIELDS)) {
            IndividualTicket ticket = ticketById.apply(f[2]);
            if (ticket == null) {
                LOGGER.warning(() -> "pass " + f[0] + " references unknown individual ticket " + f[2]);
            } else {
                passes.add(attach(f, ticket));
            }
        }
        return passes;
    }

    private static Pass attach(String[] f, IndividualTicket ticket) {
        BigDecimal price = new BigDecimal(f[3]);
        boolean consumed = Boolean.parseBoolean(f[4]);
        if (SPECIAL.equals(f[1])) {
            SpecialPass pass = new SpecialPass(f[0], price, ticket, consumed);
            ticket.setSpecialPass(pass);
            return pass;
        }
        SpeedPass pass = new SpeedPass(f[0], price, ticket, consumed);
        ticket.addSpeedPass(pass);
        return pass;
    }

    @Override
    public void save(Collection<IndividualTicket> tickets) {
        TextFile.writeRecords(file, tickets.stream()
                .flatMap(t -> t.getPasses().stream())
                .map(p -> TextFile.join(p.getId(), p instanceof SpecialPass ? SPECIAL : SPEED,
                        p.getTicket().getId(), p.getPrice().toPlainString(), String.valueOf(p.isConsumed())))
                .toList());
    }
}
