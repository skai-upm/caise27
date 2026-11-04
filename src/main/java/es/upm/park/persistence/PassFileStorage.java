package es.upm.park.persistence;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/** Stores passes: {@code SPEED|SPECIAL;id;ticketId;price;consumed}, one per line. */
public class PassFileStorage {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 5;

    private final Path file;

    public PassFileStorage(Path file) {
        this.file = file;
    }

    /** Loads passes, linking each to the ticket returned by {@code ticketById} (null if gone). */
    public List<Pass> load(Function<String, IndividualTicket> ticketById) {
        List<Pass> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                Pass p = parse(line, ticketById);
                if (p != null) {
                    result.add(p);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read passes file " + file, e);
        }
        return result;
    }

    public void save(Collection<Pass> passes) {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(String.join(SEPARATOR, p.kind(), p.getId(), p.getTicketId(), p.getPrice().toPlainString(),
                    String.valueOf(p.isConsumed())));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write passes file " + file, e);
        }
    }

    private static Pass parse(String line, Function<String, IndividualTicket> ticketById) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] f = line.split(SEPARATOR, -1);
        if (f.length != FIELD_COUNT || f[1].isBlank() || !(f[4].equals("true") || f[4].equals("false"))) {
            return null;
        }
        try {
            BigDecimal price = new BigDecimal(f[3]);
            IndividualTicket ticket = ticketById.apply(f[2]);
            boolean consumed = Boolean.parseBoolean(f[4]);
            return switch (f[0]) {
                case SpeedPass.KIND -> new SpeedPass(f[1], ticket, f[2], price, consumed);
                case SpecialPass.KIND -> new SpecialPass(f[1], ticket, f[2], price, consumed);
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
