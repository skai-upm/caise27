package es.upm.etsisi.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
        int status = run(args, System.in, System.out, System.err, Path.of(""), LocalDate.now());
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path baseDir, LocalDate today) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;
        if (!fly) {
            return execute(commandFile, stdin, out, err, baseDir, today);
        }
        try (TemporaryStorage storage = TemporaryStorage.create(baseDir, LocalDateTime.now())) {
            return execute(commandFile, stdin, out, err, storage.getDirectory(), today);
        } catch (IOException e) {
            err.println("ERROR: Temporary storage failure: " + e.getMessage());
            return 1;
        }
    }

    private static int execute(String commandFile, InputStream stdin, PrintStream out, PrintStream err,
                               Path dataDir, LocalDate today) {
        try {
            Park park = Park.getInstance();
            park.start(new ParkRepository(dataDir), today);
            CommandProcessor processor = new CommandProcessor(park);
            if (commandFile == null) {
                processor.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            } else {
                processor.runFile(Path.of(commandFile), out);
            }
            return 0;
        } catch (ParkException | IOException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
    }
}
