package es.upm.parque;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import es.upm.parque.command.CommandProcessor;
import es.upm.parque.repository.UserRepository;

/**
 * Application entry point: reads commands from standard input and prints their responses.
 */
public class App {

    public static void main(String[] args) throws IOException {
        CommandProcessor processor = new CommandProcessor(new UserRepository());
        PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
        run(processor, new InputStreamReader(System.in, StandardCharsets.UTF_8), console);
    }

    static void run(CommandProcessor processor, Reader input, PrintStream output) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        String line;
        while ((line = reader.readLine()) != null) {
            String response = processor.process(line);
            if (response != null) {
                output.println(response);
            }
        }
    }
}
