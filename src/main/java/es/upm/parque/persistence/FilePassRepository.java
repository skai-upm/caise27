package es.upm.parque.persistence;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.repository.PassRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Stores passes in a text file, one per line: {@code SPEED|SPECIAL;IdPase;IdEntrada;precio;consumido}.
 */
public class FilePassRepository implements PassRepository {
    private static final String SEPARATOR = ";";
    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path file;

    public FilePassRepository(Path file) {
        this.file = file;
    }

    @Override
    public void load(Function<String, IndividualTicket> ticketById) {
        if (!Files.exists(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(SEPARATOR, -1);
                IndividualTicket ticket = f.length == 5 ? ticketById.apply(f[2]) : null;
                if (ticket == null) {
                    System.err.println("Ignoring malformed or orphan pass line: " + line);
                    continue;
                }
                try {
                    BigDecimal price = new BigDecimal(f[3]);
                    boolean consumed = Boolean.parseBoolean(f[4]);
                    switch (f[0]) {
                        case SPEED -> ticket.addSpeedPass(new SpeedPass(f[1], price, consumed));
                        case SPECIAL -> ticket.setSpecialPass(new SpecialPass(f[1], price, consumed));
                        default -> System.err.println("Ignoring pass of unknown kind: " + line);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Ignoring malformed pass line: " + line);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read passes file " + file + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void save(List<IndividualTicket> tickets) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (IndividualTicket ticket : tickets) {
                    for (Pass pass : ticket.getPasses()) {
                        String kind = pass instanceof SpecialPass ? SPECIAL : SPEED;
                        writer.write(String.join(SEPARATOR, kind, pass.getId(), ticket.getId(),
                                pass.getPrice().toPlainString(), Boolean.toString(pass.isConsumed())));
                        writer.newLine();
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write passes file " + file + ": " + e.getMessage(), e);
        }
    }
}
