package es.upm.parque.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.PersonalPass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.Ticket;

/**
 * Stores passes in a text file: one pass per line as ticketId;passId;SPEED|SPECIAL;price;consumed.
 */
public class TextFilePassRepository implements PassRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 5;

    private final Path file;

    public TextFilePassRepository(Path file) {
        this.file = file;
    }

    @Override
    public void load(Function<String, IndividualTicket> ticketById) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] parts = line.split(SEPARATOR, -1);
            IndividualTicket ticket = parts.length == FIELDS ? ticketById.apply(parts[0]) : null;
            BigDecimal price = ticket != null ? Ticket.parsePrice(parts[3]) : null;
            if (price == null || parts[1].isBlank()) {
                continue;
            }
            boolean consumed = Boolean.parseBoolean(parts[4]);
            if (SpeedPass.KIND.equals(parts[2])) {
                ticket.addSpeedPass(new SpeedPass(parts[1], price, consumed));
            } else if (SpecialPass.KIND.equals(parts[2])) {
                ticket.setSpecialPass(new SpecialPass(parts[1], price, consumed));
            }
        }
    }

    @Override
    public void save(Collection<IndividualTicket> tickets) throws IOException {
        List<String> lines = new ArrayList<>();
        for (IndividualTicket ticket : tickets) {
            for (PersonalPass pass : ticket.getPasses()) {
                lines.add(String.join(SEPARATOR, ticket.getId(), pass.getId(), pass.getKind(),
                        pass.getPrice().toPlainString(), String.valueOf(pass.isConsumed())));
            }
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
