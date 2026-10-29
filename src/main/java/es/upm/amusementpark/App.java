package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard
 * input; otherwise the first argument is the path of a command file.
 */
public class App {

    public static void main(String[] args) {
        PrintStream out = System.out;
        CommandProcessor processor = new CommandProcessor(out);
        if (args.length == 0) {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                processor.run(in);
            } catch (IOException e) {
                out.println("Error: could not read standard input: " + e.getMessage());
            }
        } else {
            Path file = Path.of(args[0]);
            try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                processor.run(in);
            } catch (IOException e) {
                out.println("Error: could not read command file: " + args[0]);
            }
        }
    }
}
