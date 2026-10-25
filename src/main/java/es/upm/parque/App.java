package es.upm.parque;

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
import java.util.stream.Stream;

/**
 * Amusement Park application entry point.
 * Usage: [--fly] [commandFile]. Without a command file, commands are read from standard input.
 * With --fly, all persistence files live in a temporary tmp/&lt;timestamp&gt;/ directory removed at the end.
 */
public class App {

    static final String FLY_OPTION = "--fly";
    private static final DateTimeFormatter TMP_NAME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void main(String[] args) {
        run(args, Path.of(""), System.out);
    }

    /**
     * Runs the application with persistence relative to the given base directory.
     */
    static void run(String[] args, Path baseDir, PrintStream out) {
        boolean fly = args.length > 0 && FLY_OPTION.equals(args[0]);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;

        Path dataDir = baseDir.resolve("data");
        Thread cleanup = null;
        if (fly) {
            try {
                dataDir = createTemporaryDirectory(baseDir.resolve("tmp"));
            } catch (IOException e) {
                out.println("ERROR cannot create temporary directory: " + e.getMessage());
                return;
            }
            Path tmp = dataDir;
            cleanup = new Thread(() -> deleteRecursively(tmp));
            Runtime.getRuntime().addShutdownHook(cleanup);
        }
        try {
            runWithData(commandFile, dataDir, out);
        } finally {
            if (fly) {
                deleteRecursively(dataDir);
                Runtime.getRuntime().removeShutdownHook(cleanup);
            }
        }
    }

    private static void runWithData(String commandFile, Path dataDir, PrintStream out) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(ParkController.load(dataDir));
        } catch (IOException | RuntimeException e) {
            out.println("ERROR could not load stored data: " + e.getMessage());
            return;
        }
        if (commandFile == null) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            processLines(stdin, processor, out);
        } else {
            Path path = Path.of(commandFile);
            if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
                out.println("ERROR cannot open command file: " + commandFile);
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                processLines(reader, processor, out);
            } catch (IOException e) {
                out.println("ERROR cannot read command file: " + commandFile);
            }
        }
    }

    private static void processLines(BufferedReader reader, CommandProcessor processor, PrintStream out) {
        try {
            String line;
            while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                out.println(processor.process(line));
            }
        } catch (IOException e) {
            out.println("ERROR reading commands: " + e.getMessage());
        }
    }

    static Path createTemporaryDirectory(Path tmpRoot) throws IOException {
        String name = LocalDateTime.now().format(TMP_NAME);
        Path dir = tmpRoot.resolve(name);
        for (int i = 1; Files.exists(dir); i++) {
            dir = tmpRoot.resolve(name + "_" + i);
        }
        return Files.createDirectories(dir);
    }

    static void deleteRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    System.err.println("WARNING could not delete " + p);
                }
            });
        } catch (IOException e) {
            System.err.println("WARNING could not delete " + dir);
        }
    }
}
