package es.upm.etsisi.park;

import es.upm.etsisi.park.business.controller.Park;
import es.upm.etsisi.park.business.exception.PersistenceException;
import es.upm.etsisi.park.business.security.BCryptPasswordHasher;
import es.upm.etsisi.park.persistence.DataPaths;
import es.upm.etsisi.park.persistence.Repositories;
import es.upm.etsisi.park.persistence.file.FileRepositories;
import es.upm.etsisi.park.presentation.CommandProcessor;
import es.upm.etsisi.park.presentation.CommandRunner;
import es.upm.etsisi.park.presentation.help.ExtendedHelpFormatter;
import es.upm.etsisi.park.presentation.help.HelpGenerator;
import es.upm.etsisi.park.presentation.help.HelpOption;
import es.upm.etsisi.park.presentation.help.SimpleHelpFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Application entry point and composition root: creates the repositories for the chosen
 * data directory, configures the controllers and builds the presentation objects.
 * <ul>
 *   <li>no arguments: interactive mode, data in the working directory;</li>
 *   <li>{@code <file>}: command file mode;</li>
 *   <li>{@code --fly [<file>]}: same, with all data in a temporary directory deleted at the end.</li>
 * </ul>
 */
public class App {

    static final String FLY_OPTION = "--fly";
    static final String TEMP_ROOT = "tmp";
    private static final DateTimeFormatter TEMP_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        System.exit(run(args, Path.of(""), System.in, System.out, System.err));
    }

    static int run(String[] args, Path workDir, InputStream in, PrintStream out, PrintStream err) {
        boolean fly = args.length > 0 && FLY_OPTION.equals(args[0]);
        String commandFile = argumentOrNull(args, fly ? 1 : 0);

        Path dataDir = workDir;
        try {
            if (fly) {
                dataDir = createTemporaryDirectory(workDir);
            }
            return execute(dataDir, commandFile, in, out, err);
        } catch (IOException | PersistenceException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        } finally {
            if (fly && !dataDir.equals(workDir)) {
                deleteTemporaryDirectory(dataDir, err);
            }
        }
    }

    private static int execute(Path dataDir, String commandFile, InputStream in, PrintStream out, PrintStream err)
            throws IOException {
        Repositories repositories = FileRepositories.open(new DataPaths(dataDir));
        Park park = Park.getInstance();
        park.configure(repositories, new BCryptPasswordHasher());
        CommandRunner runner = new CommandRunner(new CommandProcessor(park, helpOptions(), out));

        if (commandFile == null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                runner.run(reader);
            }
            return 0;
        }

        Path file = Path.of(commandFile);
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            err.println("ERROR: Command file not found or not readable: " + commandFile);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            runner.run(reader);
        }
        return 0;
    }

    public static List<HelpOption> helpOptions() {
        return List.of(
                new HelpOption("", "Lists the syntax of every command.", new HelpGenerator(new SimpleHelpFormatter())),
                new HelpOption("EXTENDED", "Lists every command with a description of its purpose.",
                        new HelpGenerator(new ExtendedHelpFormatter())));
    }

    private static String argumentOrNull(String[] args, int index) {
        return args.length > index ? args[index] : null;
    }

    static Path createTemporaryDirectory(Path workDir) throws IOException {
        String stamp = LocalDateTime.now(ZoneId.systemDefault()).format(TEMP_DIR_FORMAT);
        Path root = workDir.resolve(TEMP_ROOT);
        Path dir = root.resolve(stamp);
        for (int suffix = 1; Files.exists(dir); suffix++) {
            dir = root.resolve(stamp + "_" + suffix);
        }
        return Files.createDirectories(dir);
    }

    private static void deleteTemporaryDirectory(Path dir, PrintStream err) {
        try (Stream<Path> walk = Files.walk(dir)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
            Path root = dir.getParent();
            try (Stream<Path> remaining = Files.list(root)) {
                if (remaining.findAny().isEmpty()) {
                    Files.delete(root);
                }
            }
        } catch (IOException e) {
            err.println("WARNING: Could not delete temporary directory " + dir);
        }
    }
}
