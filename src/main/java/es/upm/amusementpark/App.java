package es.upm.amusementpark;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.persistence.UserFileStorage;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.UserService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Amusement Park application entry point.
 */
public class App {

    private static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        CommandProcessor processor = new CommandProcessor(new UserService(new UserRepository(new UserFileStorage(USERS_FILE))));
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        new InteractiveSession(stdin, System.out, processor).run();
    }
}
