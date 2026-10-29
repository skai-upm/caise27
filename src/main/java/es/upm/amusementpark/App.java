package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Application entry point: {@code [--fly] [commandFile]}. Without a command
 * file, commands are read from standard input. With {@code --fly}, the park
 * data lives in a temporary directory that is deleted at the end.
 */
public class App {

    static final String FLY = "--fly";
    static final String DATA_DIRECTORY = "data";
    static final String TEMP_DIRECTORY = "tmp";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        run(args, Path.of(""), System.in, System.out);
    }

    static void run(String[] args, Path baseDirectory, InputStream in, PrintStream out) {
        boolean fly = args.length > 0 && args[0].equals(FLY);
        String commandFile = fly ? (args.length > 1 ? args[1] : null) : (args.length > 0 ? args[0] : null);
        Path dataDirectory = null;
        try {
            dataDirectory = fly ? createFlyDirectory(baseDirectory) : baseDirectory.resolve(DATA_DIRECTORY);
            ParkController park = ParkController.getInstance();
            park.reset();
            ParkStorage storage = new ParkStorage(dataDirectory);
            List<String> skipped = storage.load(park);
            for (String line : skipped) {
                out.println("Warning: skipped unreadable stored data: " + line);
            }
            CommandProcessor processor = new CommandProcessor(out);
            processor.setAfterCommand(() -> storage.save(park));
            process(processor, commandFile, in, out);
        } catch (IOException | UncheckedIOException e) {
            out.println("Error: could not access park data: " + e.getMessage());
        } finally {
            if (fly && dataDirectory != null) {
                deleteRecursively(dataDirectory, out);
            }
        }
    }

    private static void process(CommandProcessor processor, String commandFile, InputStream in, PrintStream out) {
        if (commandFile == null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                processor.run(reader);
            } catch (IOException e) {
                out.println("Error: could not read standard input: " + e.getMessage());
            }
        } else {
            try (BufferedReader reader = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
                processor.run(reader);
            } catch (IOException e) {
                out.println("Error: could not read command file: " + commandFile);
            }
        }
    }

    private static Path createFlyDirectory(Path baseDirectory) throws IOException {
        Path parent = baseDirectory.resolve(TEMP_DIRECTORY);
        Files.createDirectories(parent);
        String name = LocalDateTime.now().format(TIMESTAMP);
        Path directory = parent.resolve(name);
        for (int suffix = 1; Files.exists(directory); suffix++) {
            directory = parent.resolve(name + "_" + suffix);
        }
        return Files.createDirectory(directory);
    }

    private static void deleteRecursively(Path directory, PrintStream out) {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            out.println("Error: could not delete temporary data: " + directory);
        }
    }
}
