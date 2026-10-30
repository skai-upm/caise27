package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Application entry point. Supported forms: no arguments (interactive), {@code <file>}, {@code --fly} and
 * {@code --fly <file>}. With --fly every repository works inside a temporary directory deleted at the end.
 */
public class App {

    static final String FLY = "--fly";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, Path.of(""));
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, Path baseDirectory) {
        boolean fly = args.length > 0 && args[0].equals(FLY);
        int fileIndex = fly ? 1 : 0;
        String commandFile = args.length > fileIndex ? args[fileIndex] : null;

        TemporaryDataDirectory temporary = null;
        try {
            Path dataDirectory = baseDirectory;
            if (fly) {
                temporary = TemporaryDataDirectory.create(baseDirectory);
                dataDirectory = temporary.getPath();
            }
            Park.getInstance().open(dataDirectory);
            CommandRunner runner = new CommandRunner(new CommandProcessor(), out);
            if (commandFile == null) {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
                return 0;
            }
            return runner.runFile(Path.of(commandFile)) ? 0 : 1;
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
            return 1;
        } finally {
            if (temporary != null) {
                temporary.delete();
            }
        }
    }
}
