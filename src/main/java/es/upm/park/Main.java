package es.upm.park;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import es.upm.park.command.CommandProcessor;
import es.upm.park.command.CommandRunner;
import es.upm.park.controller.ParkController;
import es.upm.park.persistence.DataDirectory;
import es.upm.park.persistence.PersistenceException;

/**
 * Entry point of the Amusement Park application. It only interprets the
 * arguments, wires the components and selects the command source.
 * Usage: {@code [--fly] [commandFile]}.
 */
public final class Main {

    static final String BANNER = "Amusement Park application started";
    static final String FLY = "--fly";
    static final String DATA_DIR_PROPERTY = "park.data.dir";
    static final String TMP_DIR_PROPERTY = "park.tmp.dir";

    private Main() {
    }

    /**
     * Starts the application.
     *
     * @param args optional {@code --fly} followed by an optional command
     *             file path; without a file commands are read from standard input
     */
    public static void main(String[] args) {
        System.out.println(BANNER);
        boolean fly = args.length > 0 && FLY.equals(args[0]);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;
        try (DataDirectory data = fly
                ? DataDirectory.temporary(Path.of(System.getProperty(TMP_DIR_PROPERTY, "tmp")), LocalDateTime.now())
                : DataDirectory.permanent(Path.of(System.getProperty(DATA_DIR_PROPERTY, ".")))) {
            run(data, commandFile);
        } catch (PersistenceException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void run(DataDirectory data, String commandFile) {
        ParkController park = ParkController.getInstance();
        try {
            park.start(data.path(), LocalDate.now());
        } catch (PersistenceException e) {
            System.out.println("Error: cannot load park data: " + e.getMessage());
            return;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(park), System.out);
        if (commandFile != null) {
            runner.runFile(commandFile);
        } else {
            runner.runStandardInput(System.in);
        }
    }
}
