package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;

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
 * Stores passes in a text file, one per line:
 * {@code SPEED|SPECIAL;IdPase;IdTicket;Precio;Consumido}.
 */
public class PassFileRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 5;
    private static final String SPEED = "SPEED";
    private static final String SPECIAL = "SPECIAL";

    private final Path file;

    public PassFileRepository(Path file) {
        this.file = file;
    }

    /**
     * Loads all passes; a missing file means there are none.
     *
     * @param ticketById returns the existing individual ticket with an id, or {@code null}
     */
    public List<Pass> load(Function<String, IndividualTicket> ticketById) throws IOException {
        List<Pass> passes = new ArrayList<>();
        if (!Files.exists(file)) {
            return passes;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            try {
                if (f.length != FIELD_COUNT || !(f[4].equals("true") || f[4].equals("false"))) {
                    throw new IllegalArgumentException("malformed");
                }
                IndividualTicket ticket = ticketById.apply(f[2]);
                if (ticket == null) {
                    throw new IllegalArgumentException("unknown ticket " + f[2]);
                }
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                switch (f[0]) {
                    case SPEED -> passes.add(new SpeedPass(f[1], price, ticket, consumed));
                    case SPECIAL -> passes.add(new SpecialPass(f[1], price, ticket, consumed));
                    default -> throw new IllegalArgumentException("unknown pass type " + f[0]);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping pass record at line " + lineNumber + " of " + file + ": " + e.getMessage());
            }
        }
        return passes;
    }

    /** Replaces the file content with the given passes. */
    public void save(Collection<Pass> passes) throws IOException {
        List<String> lines = new ArrayList<>(passes.size());
        for (Pass p : passes) {
            String type = p instanceof SpecialPass ? SPECIAL : SPEED;
            lines.add(String.join(SEPARATOR, type, p.getId(), p.getTicket().getId(),
                    p.getPrice().toPlainString(), String.valueOf(p.isConsumed())));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
