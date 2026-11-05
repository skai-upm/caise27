package es.upm.amusementpark;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.UserService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Amusement Park application entry point.
 */
public class App {

    public static void main(String[] args) {
        CommandProcessor processor = new CommandProcessor(new UserService(new UserRepository()));
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        new InteractiveSession(stdin, System.out, processor).run();
    }
}
