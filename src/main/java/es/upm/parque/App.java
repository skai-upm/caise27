package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Amusement park application entry point.
 * Without a file argument it reads commands from standard input; otherwise it reads them from the
 * given file. Both modes share the same command processing. With {@code --fly}, all persistence
 * files live in a temporary directory that is deleted when the application ends.
 */
public class App {

    public static final String FLY_OPTION = "--fly";
    public static final Path DATA_DIRECTORY = Path.of("data");
    public static final Path TEMPORARY_BASE = Path.of("tmp");

    public static void main(String[] args) {
        boolean fly = false;
        String commandFile = null;
        for (String arg : args) {
            if (FLY_OPTION.equals(arg)) {
                fly = true;
            } else if (commandFile == null) {
                commandFile = arg;
            }
        }

        TemporaryStorage temporary = null;
        Thread cleanup = null;
        Path dataDirectory = DATA_DIRECTORY;
        try {
            if (fly) {
                temporary = TemporaryStorage.create(TEMPORARY_BASE);
                cleanup = new Thread(temporary::delete);
                Runtime.getRuntime().addShutdownHook(cleanup);
                dataDirectory = temporary.getDirectory();
            }
            run(dataDirectory, commandFile);
        } catch (UncheckedIOException e) {
            System.out.println("ERROR: " + e.getMessage());
        } finally {
            if (temporary != null) {
                removeHook(cleanup);
                temporary.delete();
            }
        }
    }

    private static void run(Path dataDirectory, String commandFile) {
        CommandProcessor processor = new CommandProcessor(ParkController.load(dataDirectory));
        if (commandFile == null) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            run(stdin, processor, System.out, false);
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            run(reader, processor, System.out, true);
        } catch (IOException e) {
            System.out.println("ERROR: cannot read command file '" + commandFile + "': " + describe(e));
        }
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     * Read errors are reported and stop processing.
     *
     * @param echo whether each command is printed before its result (useful for file input)
     */
    public static void run(BufferedReader input, CommandProcessor processor, PrintStream out, boolean echo) {
        try {
            String line;
            while ((line = input.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (echo) {
                    out.println("> " + line);
                }
                CommandProcessor.Result result = processor.process(line);
                out.println(result.output());
                if (result.exit()) {
                    return;
                }
            }
        } catch (IOException e) {
            out.println("ERROR: cannot read commands: " + describe(e));
        }
    }

    private static void removeHook(Thread hook) {
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException e) {
            // The JVM is already shutting down; the hook performs the deletion.
        }
    }

    private static String describe(IOException e) {
        if (e instanceof java.nio.file.NoSuchFileException) {
            return "file does not exist";
        }
        if (e instanceof java.nio.file.AccessDeniedException) {
            return "permission denied";
        }
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
