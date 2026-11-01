package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Application entry point: {@code [--fly] [commandFile]}. Without a command file,
 * commands are read from standard input. With {@code --fly}, all data is kept in
 * a per-run temporary directory that is deleted when the application ends.
 */
public class App {

    static final String FLY_FLAG = "--fly";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, Path.of(""));
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Runs the application with its data under {@code baseDir} and returns the exit status. */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path baseDir) {
        boolean fly = args.length > 0 && FLY_FLAG.equals(args[0]);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;

        StorageConfig storage;
        try {
            storage = fly ? StorageConfig.createTemporary(baseDir, LocalDateTime.now()) : new StorageConfig(baseDir);
        } catch (IOException e) {
            err.println("ERROR: cannot create temporary storage: " + e.getMessage());
            return 1;
        }
        try {
            return runWith(storage, commandFile, in, out, err);
        } finally {
            if (fly) {
                deleteTemporary(storage, err);
            }
        }
    }

    private static int runWith(StorageConfig storage, String commandFile, InputStream in, PrintStream out,
                               PrintStream err) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new Park(storage));
        } catch (IOException e) {
            err.println("ERROR: cannot load data from " + storage.directory() + ": " + e.getMessage());
            return 1;
        }
        if (commandFile == null) {
            return runInteractive(processor, in, out, err);
        }
        return runFile(processor, commandFile, out, err);
    }

    private static int runInteractive(CommandProcessor processor, InputStream in, PrintStream out, PrintStream err) {
        try {
            processor.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), out);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read standard input: " + e.getMessage());
            return 1;
        }
    }

    private static int runFile(CommandProcessor processor, String fileName, PrintStream out, PrintStream err) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName), StandardCharsets.UTF_8)) {
            processor.run(reader, out);
            return 0;
        } catch (IOException | InvalidPathException e) {
            err.println("ERROR: cannot read command file '" + fileName + "': " + describe(e));
            return 1;
        }
    }

    private static void deleteTemporary(StorageConfig storage, PrintStream err) {
        try {
            storage.deleteDirectory();
        } catch (IOException e) {
            err.println("ERROR: cannot delete temporary storage " + storage.directory() + ": " + e.getMessage());
        }
    }

    private static String describe(Exception e) {
        if (e instanceof NoSuchFileException) {
            return "file not found";
        }
        if (e instanceof AccessDeniedException) {
            return "permission denied";
        }
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
