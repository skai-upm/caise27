package es.upm.amusementpark;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.persistence.DataDirectories;
import es.upm.amusementpark.persistence.ParkStorage;
import es.upm.amusementpark.persistence.PersistenceException;
import es.upm.amusementpark.presentation.CommandProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Application entry point and composition root: {@code [--fly] [commandFile]}.
 * Without a command file, commands are read from standard input. With
 * {@code --fly}, the park data lives in a temporary directory that is deleted
 * at the end.
 */
public class App {

    static final String FLY = "--fly";
    static final String DATA_DIRECTORY = DataDirectories.DATA_DIRECTORY;
    static final String TEMP_DIRECTORY = DataDirectories.TEMP_DIRECTORY;

    public static void main(String[] args) {
        run(args, Path.of(""), System.in, System.out);
    }

    static void run(String[] args, Path baseDirectory, InputStream in, PrintStream out) {
        boolean fly = args.length > 0 && args[0].equals(FLY);
        String commandFile = fly ? (args.length > 1 ? args[1] : null) : (args.length > 0 ? args[0] : null);
        Path dataDirectory = null;
        try {
            dataDirectory = fly ? DataDirectories.createTemporary(baseDirectory)
                    : DataDirectories.permanent(baseDirectory);
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
        } catch (PersistenceException e) {
            out.println("Error: could not access park data: " + e.getMessage());
        } finally {
            if (fly && dataDirectory != null) {
                try {
                    DataDirectories.delete(dataDirectory);
                } catch (PersistenceException e) {
                    out.println("Error: " + e.getMessage());
                }
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
}
