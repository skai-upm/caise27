package es.upm.park.persistence;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Stores passes, one per line: {@code SPEED|SPECIAL;Id;IdTicket;Precio;true|false}. Lines whose
 * ticket can no longer be found are kept verbatim as history.
 */
public class PassFileStore implements PassRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 5;

    /** Content of the file: linked passes plus preserved historical lines. */
    public record Contents(List<Pass> passes, List<String> historicalLines) {
    }

    private final Path file;

    public PassFileStore(Path file) {
        this.file = file;
    }

    @Override
    public Contents load(Function<String, Optional<IndividualTicket>> ticketById) throws IOException {
        List<Pass> passes = new ArrayList<>();
        List<String> history = new ArrayList<>();
        if (!Files.exists(file)) {
            return new Contents(passes, history);
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            Pass pass = parse(line.split(SEPARATOR, -1), ticketById);
            if (pass != null) {
                passes.add(pass);
            } else {
                history.add(line);
            }
        }
        return new Contents(passes, history);
    }

    private static Pass parse(String[] f, Function<String, Optional<IndividualTicket>> ticketById) {
        if (f.length != FIELD_COUNT || f[1].isBlank()
                || !(f[4].equals("true") || f[4].equals("false"))) {
            return null;
        }
        Optional<IndividualTicket> ticket = ticketById.apply(f[2]);
        if (ticket.isEmpty()) {
            return null;
        }
        BigDecimal price;
        try {
            price = new BigDecimal(f[3]);
        } catch (NumberFormatException e) {
            return null;
        }
        boolean consumed = Boolean.parseBoolean(f[4]);
        return switch (f[0]) {
            case SpeedPass.TYPE_KEY -> new SpeedPass(f[1], price, ticket.get(), consumed);
            case SpecialPass.TYPE_KEY -> new SpecialPass(f[1], price, ticket.get(), consumed);
            default -> null;
        };
    }

    /** Identifier stored in a pass line, or {@code null}. */
    public static String idOf(String line) {
        String[] f = line.split(SEPARATOR, -1);
        return f.length > 1 ? f[1] : null;
    }

    @Override
    public void save(List<Pass> passes, List<String> historicalLines) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Pass p : passes) {
            lines.add(String.join(SEPARATOR, p.kind(), p.getId(), p.getTicket().getId(),
                    p.getPrice().toPlainString(), Boolean.toString(p.isConsumed())));
        }
        lines.addAll(historicalLines);
        Path absolute = file.toAbsolutePath();
        Path dir = absolute.getParent();
        if (dir != null) {
            Files.createDirectories(dir);
        }
        Path tmp = Files.createTempFile(dir, absolute.getFileName().toString(), ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
