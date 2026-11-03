package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores passes, one per line: {@code Id;Tipo;IdTicket;Precio;Consumido} with {@code Tipo} SPEED or SPECIAL.
 * Records whose ticket no longer exists are skipped on load. Only class touching the pass file.
 */
public class PassFileStorage implements PassRepository {

    public static final String DEFAULT_FILE = "passes.txt";
    private static final String SEPARATOR = ";";
    private static final int FIELDS = 5;

    private final Path file;

    public PassFileStorage(Path file) {
        this.file = file;
    }

    @Override
    public List<Pass> load(Function<String, Optional<Ticket>> ticketById) {
        List<Pass> passes = new ArrayList<>();
        if (!Files.exists(file)) {
            return passes;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read pass file " + file, e);
        }
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            try {
                parse(line, ticketById).ifPresentOrElse(passes::add,
                        () -> System.err.println("Skipping pass of an unknown ticket: " + line));
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping malformed pass record: " + line);
            }
        }
        return passes;
    }

    private static Optional<Pass> parse(String line, Function<String, Optional<Ticket>> ticketById) {
        String[] f = line.split(SEPARATOR, -1);
        if (f.length != FIELDS || !(f[4].equals("true") || f[4].equals("false"))) {
            throw new IllegalArgumentException("malformed");
        }
        BigDecimal price = new BigDecimal(f[3]);
        boolean consumed = Boolean.parseBoolean(f[4]);
        return ticketById.apply(f[2]).map(ticket -> switch (f[1]) {
            case SpeedPass.TYPE -> new SpeedPass(f[0], ticket, price, consumed);
            case SpecialPass.TYPE -> new SpecialPass(f[0], ticket, price, consumed);
            default -> throw new IllegalArgumentException("unknown pass type " + f[1]);
        });
    }

    @Override
    public void save(List<Pass> passes) {
        List<String> lines = new ArrayList<>(passes.size());
        for (Pass p : passes) {
            lines.add(String.join(SEPARATOR, p.getId(), p.getType(), p.getTicket().getId(), p.getPrice().toPlainString(),
                    String.valueOf(p.isConsumed())));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write pass file " + file, e);
        }
    }
}
