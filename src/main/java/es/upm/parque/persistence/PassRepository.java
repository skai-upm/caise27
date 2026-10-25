package es.upm.parque.persistence;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.Ticket;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Stores personal passes in a text file: id;SPEED|SPECIAL;ticketId;price;consumed
 */
public class PassRepository {

    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path file;

    public PassRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads the passes and attaches them to their individual tickets.
     */
    public void load(Function<String, Ticket> ticketById) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(";", -1);
            if (f.length != 5 || !(ticketById.apply(f[2]) instanceof IndividualTicket ticket)) {
                System.err.println("WARNING skipping malformed pass line: " + line);
                continue;
            }
            BigDecimal price = new BigDecimal(f[3]);
            boolean consumed = Boolean.parseBoolean(f[4]);
            if (SPEED.equals(f[1])) {
                ticket.addSpeedPass(new SpeedPass(f[0], price, consumed));
            } else if (SPECIAL.equals(f[1])) {
                ticket.setSpecialPass(new SpecialPass(f[0], price, consumed));
            } else {
                System.err.println("WARNING skipping pass of unknown type: " + line);
            }
        }
    }

    /**
     * Rewrites the file with the passes of the given individual tickets.
     */
    public void save(List<IndividualTicket> tickets) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (IndividualTicket t : tickets) {
                for (Pass p : t.getPasses()) {
                    String type = p instanceof SpecialPass ? SPECIAL : SPEED;
                    writer.write(String.join(";", p.getId(), type, t.getId(), p.getPrice().toPlainString(),
                            String.valueOf(p.isConsumed())));
                    writer.newLine();
                }
            }
        }
    }
}
