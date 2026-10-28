package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.persistence.PersistenceException;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.persistence.ParkStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Amusement park application entry point.
 *
 * <p>Supported forms: no arguments (interactive), {@code <file>} (command file),
 * {@code --fly} and {@code --fly <file>} (same, with temporary persistence).
 */
public class App {

    public static final String FLY = "--fly";
    static final String DATA_DIRECTORY = "data";
    static final String TEMP_DIRECTORY = "tmp";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        run(args, System.in, System.out, Path.of(""));
    }

    /**
     * Runs one execution with {@code workDir} as the base directory for persistent files.
     */
    static void run(String[] args, InputStream stdin, PrintStream out, Path workDir) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        String file = fly ? (args.length > 1 ? args[1] : null) : (args.length > 0 ? args[0] : null);
        Path storageDirectory;
        try {
            storageDirectory = fly ? createTemporaryDirectory(workDir) : workDir.resolve(DATA_DIRECTORY);
        } catch (IOException e) {
            out.println("ERROR: Cannot create temporary directory: " + e.getMessage());
            return;
        }
        try {
            execute(file, stdin, out, new ParkStorage(storageDirectory));
        } finally {
            if (fly) {
                deleteRecursively(storageDirectory, out);
            }
        }
    }

    private static void execute(String file, InputStream stdin, PrintStream out, ParkStorage storage) {
        ParkController controller = ParkController.getInstance();
        controller.reset();
        try {
            storage.load(controller, out);
        } catch (PersistenceException e) {
            out.println("ERROR: Cannot load stored data: " + e.getMessage());
        }
        CommandProcessor processor = new CommandProcessor();
        processor.setAfterCommand(() -> {
            try {
                storage.save(controller);
            } catch (PersistenceException e) {
                out.println("ERROR: Cannot save data: " + e.getMessage());
            }
        });
        if (file == null) {
            try {
                processor.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            } catch (IOException e) {
                out.println("ERROR: Cannot read standard input");
            }
            return;
        }
        try (BufferedReader in = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) {
            processor.run(in, out);
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR: Cannot read command file: " + file);
        }
    }

    private static Path createTemporaryDirectory(Path workDir) throws IOException {
        String name = LocalDateTime.now().format(TIMESTAMP);
        Path directory = workDir.resolve(TEMP_DIRECTORY).resolve(name);
        for (int n = 1; Files.exists(directory); n++) {
            directory = workDir.resolve(TEMP_DIRECTORY).resolve(name + "_" + n);
        }
        return Files.createDirectories(directory);
    }

    private static void deleteRecursively(Path directory, PrintStream out) {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        } catch (IOException e) {
            out.println("ERROR: Cannot delete temporary directory " + directory);
        }
    }
}
