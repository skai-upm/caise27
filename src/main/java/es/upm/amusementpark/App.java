package es.upm.amusementpark;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.service.AttractionService;
import es.upm.amusementpark.service.GroupService;
import es.upm.amusementpark.service.TicketService;
import es.upm.amusementpark.service.UserService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Amusement Park application entry point.
 * <ul>
 *   <li>no arguments: commands from standard input;</li>
 *   <li>{@code <file>}: commands from a file;</li>
 *   <li>{@code --fly [file]}: same, persisting only in a temporary directory deleted at the end.</li>
 * </ul>
 */
public class App {

    static final String FLY = "--fly";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, Path.of("."));
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application with {@code baseDirectory} as the location of the permanent data files.
     *
     * @return the process exit status
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path baseDirectory) {
        if (args.length > 0 && FLY.equals(args[0])) {
            Path tempDirectory;
            try {
                tempDirectory = TemporaryDirectory.create(baseDirectory.resolve("tmp"));
            } catch (UncheckedIOException e) {
                err.println("ERROR: cannot create the temporary data directory");
                return 1;
            }
            try {
                return runWithData(Arrays.copyOfRange(args, 1, args.length), in, out, err, tempDirectory);
            } finally {
                TemporaryDirectory.delete(tempDirectory);
            }
        }
        return runWithData(args, in, out, err, baseDirectory);
    }

    private static int runWithData(String[] args, InputStream in, PrintStream out, PrintStream err, Path dataDirectory) {
        CommandProcessor processor;
        try {
            Park park = Park.getInstance();
            park.start(dataDirectory);
            processor = new CommandProcessor(new UserService(park.users(), park.groups(), park.tickets()),
                    new GroupService(park.users(), park.groups(), park.tickets()),
                    new TicketService(park.users(), park.tickets(), park.groups()),
                    new AttractionService(park.attractions(), park.tickets()));
        } catch (UncheckedIOException e) {
            err.println("ERROR: cannot read the park data files (users.txt, groups.txt, tickets.txt, attractions.txt) in '"
                    + dataDirectory + "'");
            return 1;
        }
        if (args.length == 0) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            new InteractiveSession(stdin, out, processor).run();
            return 0;
        }
        Path commandFile = Path.of(args[0]);
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            err.println("ERROR: cannot read command file '" + args[0] + "'");
            return 1;
        }
        try (BufferedReader file = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            new InteractiveSession(file, out, processor).run();
            return 0;
        } catch (IOException | RuntimeException e) {
            err.println("ERROR: cannot read command file '" + args[0] + "'");
            return 1;
        }
    }
}
