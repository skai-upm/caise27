package es.upm.amusementpark;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.persistence.ParkStorage;
import es.upm.amusementpark.presentation.AttractionCommands;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.GroupCommands;
import es.upm.amusementpark.presentation.InvoiceCommands;
import es.upm.amusementpark.presentation.SystemCommands;
import es.upm.amusementpark.presentation.TicketCommands;
import java.io.BufferedReader;
import java.io.IOException;
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

/**
 * Application entry point. Arguments: {@code [--fly] [commandFile]}. Without a command file, commands are read
 * from standard input. {@code --fly} uses a temporary data directory deleted at the end of the execution.
 */
public final class App {

    /** Argument enabling temporary persistence. */
    public static final String FLY = "--fly";

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private App() {
    }

    public static void main(String[] args) {
        run(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)), System.out,
                Path.of(""));
    }

    /**
     * Runs the application with explicit standard input, output and base directory (testable entry point).
     *
     * @param args command-line arguments: {@code [--fly] [commandFile]}
     * @param stdin reader used when no command file is given
     * @param out output stream
     * @param baseDir directory containing {@code data/} (normal mode) and {@code tmp/} (fly mode)
     */
    public static void run(String[] args, BufferedReader stdin, PrintStream out, Path baseDir) {
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        String commandFile = args.length > (fly ? 1 : 0) ? args[fly ? 1 : 0] : null;
        Path dataDir;
        try {
            dataDir = fly ? createFlyDirectory(baseDir) : baseDir.resolve("data");
        } catch (IOException e) {
            out.println("ERROR: Cannot create temporary data directory");
            return;
        }
        try {
            runWithStorage(new ParkStorage(dataDir), commandFile, stdin, out);
        } finally {
            if (fly) {
                deleteRecursively(dataDir, out);
            }
        }
    }

    private static void runWithStorage(ParkStorage storage, String commandFile, BufferedReader stdin,
            PrintStream out) {
        ParkController park = ParkController.getInstance();
        park.reset();
        try {
            for (String warning : storage.load(park)) {
                out.println("WARNING: Ignoring persisted record (" + warning + ")");
            }
        } catch (IOException e) {
            out.println("ERROR: Cannot read persisted data from " + storage.getDirectory());
        }
        CommandProcessor processor = new CommandProcessor(out);
        registerCommands(processor, park, out);
        processor.setAfterCommand(() -> {
            try {
                storage.save(park);
            } catch (IOException e) {
                out.println("ERROR: Cannot save data to " + storage.getDirectory());
            }
        });
        if (commandFile == null) {
            process(processor, stdin, out, "standard input");
            return;
        }
        try (BufferedReader file = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            process(processor, file, out, commandFile);
        } catch (IOException | RuntimeException e) {
            out.println("ERROR: Cannot read command file: " + commandFile);
        }
    }

    /**
     * Registers every application command in the processor. Single wiring point shared by the application and its
     * integration tests.
     *
     * @param processor shared command processor (already providing HELP and EXIT)
     * @param park central park management
     * @param out output stream
     */
    public static void registerCommands(CommandProcessor processor, ParkController park, PrintStream out) {
        SystemCommands.register(processor, park, out);
        GroupCommands.register(processor, park, out);
        TicketCommands.register(processor, park, out);
        AttractionCommands.register(processor, park, out);
        InvoiceCommands.register(processor, park, out);
    }

    private static Path createFlyDirectory(Path baseDir) throws IOException {
        Path tmp = baseDir.resolve("tmp");
        String name = LocalDateTime.now().format(TIMESTAMP);
        Path dir = tmp.resolve(name);
        for (int n = 1; Files.exists(dir); n++) {
            dir = tmp.resolve(name + "-" + n);
        }
        return Files.createDirectories(dir);
    }

    private static void deleteRecursively(Path dir, PrintStream out) {
        try (Stream<Path> paths = Files.walk(dir)) {
            List<Path> all = paths.sorted(Comparator.reverseOrder()).toList();
            for (Path path : all) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            out.println("ERROR: Cannot delete temporary directory " + dir);
        }
    }

    private static void process(CommandProcessor processor, BufferedReader input, PrintStream out, String source) {
        try {
            processor.run(input);
        } catch (IOException e) {
            out.println("ERROR: Cannot read commands from " + source);
        }
    }
}
