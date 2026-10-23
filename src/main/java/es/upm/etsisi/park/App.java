package es.upm.etsisi.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import es.upm.etsisi.park.business.exception.PersistenceException;
import es.upm.etsisi.park.business.security.BCryptPasswordHasher;
import es.upm.etsisi.park.persistence.text.TextRepositories;
import es.upm.etsisi.park.presentation.CommandProcessor;

/**
 * Entry point of the amusement park application.
 * <pre>
 * java -jar amusement-park.jar                  interactive, permanent data
 * java -jar amusement-park.jar input.txt        command file, permanent data
 * java -jar amusement-park.jar --fly            interactive, temporary data
 * java -jar amusement-park.jar --fly input.txt  command file, temporary data
 * </pre>
 */
public class App {

    static final String FLY = "--fly";
    /** Base directory for --fly data, relative to the working directory unless overridden. */
    static final String FLY_DIR_PROPERTY = "park.fly.dir";
    static final String DEFAULT_FLY_DIR = "tmp";

    private static final DateTimeFormatter FLY_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Runs the application and returns the process exit status. */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        int fileArgument = fly ? 1 : 0;
        String commandFile = args.length > fileArgument ? args[fileArgument] : null;

        if (commandFile != null) {
            Path path = Path.of(commandFile);
            if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
                err.println("ERROR: Command file '" + commandFile + "' does not exist or cannot be read");
                return 1;
            }
        }
        if (!fly) {
            return execute(TextRepositories.defaultDirectory(), commandFile, in, out, err);
        }

        Path flyBase = Path.of(System.getProperty(FLY_DIR_PROPERTY, DEFAULT_FLY_DIR));
        boolean baseExisted = Files.isDirectory(flyBase);
        Path flyDir;
        try {
            flyDir = createFlyDirectory(flyBase);
        } catch (IOException e) {
            err.println("ERROR: Could not create temporary directory in " + flyBase + ": " + e.getMessage());
            return 1;
        }
        try {
            return execute(flyDir, commandFile, in, out, err);
        } finally {
            deleteTree(flyDir, err);
            if (!baseExisted) {
                deleteIfEmpty(flyBase, err);
            }
        }
    }

    /** Wires the application on the repositories of {@code dataDirectory} and processes the commands. */
    private static int execute(Path dataDirectory, String commandFile, InputStream in, PrintStream out,
                               PrintStream err) {
        try {
            ApplicationContext.initialize(TextRepositories.in(dataDirectory, err), new BCryptPasswordHasher());
        } catch (PersistenceException e) {
            err.println("ERROR: Could not load park data from " + dataDirectory + ": " + e.getMessage());
            return 1;
        }
        CommandProcessor processor = ApplicationContext.commandProcessor(out);

        if (commandFile == null) {
            out.println("Amusement park - type HELP for the list of commands, EXIT to quit");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                processAll(reader, processor);
            } catch (IOException e) {
                err.println("ERROR: Could not read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        try (BufferedReader reader = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            processAll(reader, processor);
        } catch (IOException e) {
            err.println("ERROR: Could not read command file '" + commandFile + "': " + e.getMessage());
            return 1;
        }
        return 0;
    }

    private static void processAll(BufferedReader reader, CommandProcessor processor) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!processor.process(line)) {
                return;
            }
        }
    }

    /** Creates {@code base/yyyyMMdd_HHmmss}, adding a numeric suffix if that directory already exists. */
    static Path createFlyDirectory(Path base) throws IOException {
        Files.createDirectories(base);
        String stamp = LocalDateTime.now().format(FLY_STAMP);
        Path dir = base.resolve(stamp);
        for (int suffix = 2; Files.exists(dir); suffix++) {
            dir = base.resolve(stamp + "_" + suffix);
        }
        return Files.createDirectory(dir);
    }

    private static void deleteTree(Path dir, PrintStream err) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.delete(path);
            }
        } catch (IOException e) {
            err.println("WARNING: Could not delete temporary directory " + dir + ": " + e.getMessage());
        }
    }

    private static void deleteIfEmpty(Path dir, PrintStream err) {
        try (Stream<Path> entries = Files.list(dir)) {
            if (entries.findAny().isEmpty()) {
                Files.delete(dir);
            }
        } catch (IOException e) {
            err.println("WARNING: Could not delete " + dir + ": " + e.getMessage());
        }
    }
}
