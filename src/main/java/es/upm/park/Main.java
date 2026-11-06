package es.upm.park;

import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.park.command.CommandProcessor;
import es.upm.park.command.CommandRunner;
import es.upm.park.controller.ParkController;
import es.upm.park.persistence.PersistenceException;
import es.upm.park.persistence.UserFileStore;
import es.upm.park.repository.UserRepository;

/**
 * Entry point of the Amusement Park application. It only wires the
 * components and selects the command source.
 */
public final class Main {

    static final String BANNER = "Amusement Park application started";
    static final String USERS_FILE_PROPERTY = "park.users.file";
    static final String DEFAULT_USERS_FILE = "users.txt";

    private Main() {
    }

    /**
     * Starts the application.
     *
     * @param args optional path of a command file; without it commands are
     *             read interactively from standard input
     */
    public static void main(String[] args) {
        System.out.println(BANNER);
        Path usersFile = Path.of(System.getProperty(USERS_FILE_PROPERTY, DEFAULT_USERS_FILE));
        UserRepository users;
        try {
            users = new UserRepository(new UserFileStore(usersFile));
        } catch (PersistenceException e) {
            System.out.println("Error: cannot load users: " + e.getMessage());
            return;
        }
        ParkController park = ParkController.getInstance();
        park.start(users, LocalDate.now());
        CommandRunner runner = new CommandRunner(new CommandProcessor(park), System.out);
        if (args.length > 0) {
            runner.runFile(args[0]);
        } else {
            runner.runStandardInput(System.in);
        }
    }
}
