package es.upm.etsisi.park;

import es.upm.etsisi.park.business.BCryptPasswordHasher;
import es.upm.etsisi.park.business.ParkException;
import es.upm.etsisi.park.business.PasswordHasher;
import es.upm.etsisi.park.persistence.TemporaryStorage;
import es.upm.etsisi.park.persistence.TextParkRepository;
import es.upm.etsisi.park.presentation.CommandProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Application entry point.
 * <pre>
 * java -jar app.jar                   interactive, permanent data
 * java -jar app.jar input.txt         command file, permanent data
 * java -jar app.jar --fly [input.txt] same, with data in a temporary directory deleted at the end
 * </pre>
 */
public class App {

    static final String FLY = "--fly";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, Path.of(""), LocalDate.now(), new BCryptPasswordHasher());
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path baseDir,
                   LocalDate today, PasswordHasher hasher) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;
        Execution execution = new Execution(commandFile, stdin, out, err, today, hasher);
        if (!fly) {
            return execution.run(baseDir);
        }
        try (TemporaryStorage storage = TemporaryStorage.create(baseDir, LocalDateTime.now())) {
            return execution.run(storage.getDirectory());
        } catch (IOException e) {
            err.println("ERROR: Temporary storage failure: " + e.getMessage());
            return 1;
        }
    }

    private record Execution(String commandFile, InputStream stdin, PrintStream out, PrintStream err,
                             LocalDate today, PasswordHasher hasher) {

        int run(Path dataDir) {
            try {
                CommandProcessor processor = ParkApplication.commandProcessor(
                        ParkApplication.start(new TextParkRepository(dataDir), today, hasher));
                if (commandFile == null) {
                    processor.run(stdin, out);
                } else {
                    processCommandFile(processor, Path.of(commandFile));
                }
                return 0;
            } catch (ParkException | IOException | UncheckedIOException e) {
                err.println("ERROR: " + e.getMessage());
                return 1;
            }
        }

        private void processCommandFile(CommandProcessor processor, Path file) throws IOException {
            if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
                throw new IOException("Cannot read command file: " + file);
            }
            try (InputStream in = Files.newInputStream(file)) {
                processor.run(in, out);
            }
        }
    }
}
