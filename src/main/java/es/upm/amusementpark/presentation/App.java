package es.upm.amusementpark.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.persistence.StoragePaths;

/**
 * Application entry point. Without a file argument commands are read from standard
 * input; otherwise the first argument other than {@code --fly} is a command file.
 * With {@code --fly} all persistence goes to a temporary directory removed at the end.
 */
public class App {

    static final String FLY_FLAG = "--fly";

    public static void main(String[] args) {
        run(args, System.in, System.out, Path.of(""));
    }

    /**
     * Runs the application.
     *
     * @param workDir directory holding the permanent data files; {@code tmp/} is created inside it for --fly
     */
    static void run(String[] args, InputStream in, PrintStream out, Path workDir) {
        boolean fly = false;
        String commandFile = null;
        for (String arg : args) {
            if (arg.equals(FLY_FLAG)) {
                fly = true;
            } else if (commandFile == null) {
                commandFile = arg;
            }
        }

        if (!fly) {
            execute(new StoragePaths(workDir), commandFile, in, out);
            return;
        }

        StoragePaths flyPaths;
        try {
            flyPaths = StoragePaths.createTemporary(workDir.resolve("tmp"));
        } catch (IOException e) {
            out.println("Error: could not create temporary directory: " + e.getMessage());
            return;
        }
        Thread cleanupHook = new Thread(flyPaths::deleteDirectory);
        Runtime.getRuntime().addShutdownHook(cleanupHook);
        try {
            execute(flyPaths, commandFile, in, out);
        } finally {
            flyPaths.deleteDirectory();
            try {
                Runtime.getRuntime().removeShutdownHook(cleanupHook);
            } catch (IllegalStateException e) {
                // JVM already shutting down; the hook performs the same cleanup
            }
        }
    }

    private static void execute(StoragePaths paths, String commandFile, InputStream in, PrintStream out) {
        ParkController park;
        try {
            park = AppBootstrap.start(paths);
        } catch (IOException e) {
            out.println("Error: could not load data from " + paths.getDirectory().toAbsolutePath() + ": "
                    + e.getMessage());
            return;
        }
        try {
            process(new CommandProcessor(park, out), commandFile, in, out);
        } finally {
            AppBootstrap.shutdown();
        }
    }

    private static void process(CommandProcessor processor, String commandFile, InputStream in, PrintStream out) {
        if (commandFile == null) {
            try {
                processor.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                out.println("Error: could not read standard input: " + e.getMessage());
            }
            return;
        }

        Path file = Path.of(commandFile);
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            out.println("Error: command file not found or not readable: " + file);
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            processor.run(reader);
        } catch (IOException e) {
            out.println("Error: could not read command file " + file + ": " + e.getMessage());
        }
    }
}
