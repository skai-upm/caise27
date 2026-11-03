package es.upm.etsisi.park;

import es.upm.etsisi.park.business.Park;
import es.upm.etsisi.park.business.ParkRepository;
import es.upm.etsisi.park.business.User;
import es.upm.etsisi.park.persistence.TextParkRepository;
import es.upm.etsisi.park.persistence.UserRepository;
import es.upm.etsisi.park.presentation.CommandProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/** Starts the park on an isolated data directory for a test and normalizes stored data. */
final class TestParks {

    static final LocalDate TODAY = LocalDate.of(2027, 4, 15);

    private TestParks() {
    }

    static Park start(Path dataDir) throws IOException {
        return start(new TextParkRepository(dataDir));
    }

    static Park start(ParkRepository repository) throws IOException {
        processor(repository, new PrintStream(new ByteArrayOutputStream()));
        return Park.getInstance();
    }

    static CommandProcessor processor(ParkRepository repository, PrintStream out) throws IOException {
        return App.configure(repository, TODAY, out);
    }

    /** A user's stored line with the BCrypt hash replaced by {@code *}. */
    static String line(User user) {
        return mask(UserRepository.toLine(user));
    }

    /** The stored user lines with BCrypt hashes replaced by {@code *}. */
    static List<String> users(Path file) throws IOException {
        return Files.readAllLines(file).stream().map(TestParks::mask).toList();
    }

    static String mask(String line) {
        String[] f = line.split(";", -1);
        if (f.length == 7 && f[4].startsWith("$2")) {
            f[4] = "*";
        }
        return String.join(";", f);
    }
}
