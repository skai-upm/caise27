package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.repository.PassRepository;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;

/**
 * Stores passes one per line: kind;identifier;ticketIdentifier;price;consumed, where
 * kind is S (SpeedPass) or P (Special Pass).
 */
public class FilePassRepository implements PassRepository {
    private static final String SEPARATOR = ";";
    private static final String SPEED = "S";
    private static final String SPECIAL = "P";
    private static final int FIELDS = 5;

    private final Path file;
    private final List<String> warnings = new ArrayList<>();

    public FilePassRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Pass> load(Map<String, IndividualTicket> ticketsById) throws IOException {
        warnings.clear();
        List<Pass> passes = new ArrayList<>();
        if (!Files.exists(file)) {
            return passes;
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            String[] f = lines.get(i).split(SEPARATOR, -1);
            IndividualTicket ticket = f.length == FIELDS ? ticketsById.get(f[2]) : null;
            try {
                if (ticket == null) {
                    throw new IllegalArgumentException("unknown ticket");
                }
                BigDecimal price = new BigDecimal(f[3]);
                boolean consumed = Boolean.parseBoolean(f[4]);
                switch (f[0]) {
                    case SPEED -> passes.add(new SpeedPass(f[1], ticket, price, consumed));
                    case SPECIAL -> passes.add(new SpecialPass(f[1], ticket, price, consumed));
                    default -> throw new IllegalArgumentException("unknown pass kind");
                }
            } catch (IllegalArgumentException e) {
                warnings.add("skipped malformed pass line " + (i + 1) + " in " + file);
            }
        }
        return passes;
    }

    @Override
    public void save(List<Pass> passes) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            String kind = p instanceof SpecialPass ? SPECIAL : SPEED;
            lines.add(String.join(SEPARATOR, kind, p.getIdentifier(), p.getTicket().getIdentifier(),
                    p.getPrice().toPlainString(), String.valueOf(p.isConsumed())));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    @Override
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
