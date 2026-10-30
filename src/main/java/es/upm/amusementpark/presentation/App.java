package es.upm.amusementpark.presentation;

import es.upm.amusementpark.persistence.StorageLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point: starts the application and handles terminal interaction.
 * Without a file argument commands are read from standard input; otherwise from that file.
 * With {@code --fly} all persistence goes to a temporary directory deleted at the end of the run.
 */
public class App {

    static final String FLY_FLAG = "--fly";

    public static void main(String[] args) {
        run(args, Path.of(""), System.out);
    }

    /** Runs the application using {@code workDir} as the permanent storage directory. */
    static void run(String[] args, Path workDir, PrintStream out) {
        boolean fly = false;
        String commandFile = null;
        for (String arg : args) {
            if (FLY_FLAG.equals(arg)) {
                fly = true;
            } else if (commandFile == null) {
                commandFile = arg;
            }
        }
        StorageLocation storage;
        try {
            storage = fly ? StorageLocation.createTemporary(workDir.resolve("tmp")) : new StorageLocation(workDir);
        } catch (IOException e) {
            out.println("ERROR: could not create temporary directory: " + e.getMessage());
            return;
        }
        Thread cleanup = new Thread(storage::deleteIfTemporary);
        if (storage.isTemporary()) {
            Runtime.getRuntime().addShutdownHook(cleanup);
        }
        try {
            runWithStorage(storage, commandFile, out);
        } finally {
            if (storage.isTemporary()) {
                storage.deleteIfTemporary();
                removeHook(cleanup);
            }
        }
    }

    private static void runWithStorage(StorageLocation storage, String commandFile, PrintStream out) {
        CommandProcessor processor;
        try {
            processor = ApplicationBootstrap.start(storage, out);
        } catch (IOException e) {
            out.println("ERROR: could not load data from " + storage.getBaseDir() + ": " + e.getMessage());
            return;
        }
        if (commandFile == null) {
            runCommands(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)), processor, out);
        } else {
            Path path = Path.of(commandFile);
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                runCommands(reader, processor, out);
            } catch (IOException e) {
                out.println("ERROR: cannot open command file " + path);
            }
        }
    }

    private static void removeHook(Thread hook) {
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException e) {
            // JVM already shutting down: the hook itself performs the cleanup.
        }
    }

    /** Shared loop for every input mode: one command per non-blank line until EXIT or end of input. */
    static void runCommands(BufferedReader reader, CommandProcessor processor, PrintStream out) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (!processor.process(line)) {
                    return;
                }
            }
        } catch (IOException e) {
            out.println("ERROR: error reading commands: " + e.getMessage());
        }
    }
}
