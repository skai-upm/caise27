package es.upm.parque.persistence;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Stores passes in a text file, one per line: {@code type;id;ticketId;price;consumed}.
 */
public class PassRepository {

    private static final String SEPARATOR = ";";

    private final Path file;

    public PassRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all passes, resolving their tickets with {@code ticketById}. Passes of unknown tickets and malformed
     * lines are skipped.
     */
    public List<Pass> loadAll(Function<String, IndividualTicket> ticketById) throws IOException {
        List<Pass> passes = new ArrayList<>();
        if (!Files.exists(file)) {
            return passes;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Pass pass = parse(line.split(SEPARATOR, -1), ticketById);
                if (pass != null) {
                    passes.add(pass);
                }
            }
        }
        return passes;
    }

    private static Pass parse(String[] fields, Function<String, IndividualTicket> ticketById) {
        if (fields.length != 5) {
            return null;
        }
        IndividualTicket ticket = ticketById.apply(fields[2]);
        if (ticket == null) {
            return null;
        }
        try {
            BigDecimal price = new BigDecimal(fields[3]);
            boolean consumed = Boolean.parseBoolean(fields[4]);
            return switch (fields[0]) {
                case SpeedPass.TYPE -> new SpeedPass(fields[1], ticket, price, consumed);
                case SpecialPass.TYPE -> new SpecialPass(fields[1], ticket, price, consumed);
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void saveAll(Collection<Pass> passes) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Pass pass : passes) {
                writer.write(String.join(SEPARATOR, pass.getType(), pass.getId(), pass.getTicket().getId(),
                        pass.getPrice().toPlainString(), Boolean.toString(pass.isConsumed())));
                writer.newLine();
            }
        }
    }
}
