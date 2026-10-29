package es.upm.amusementpark;

import java.io.PrintStream;
import java.nio.file.Path;

import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.text.TextAttractionRepository;
import es.upm.amusementpark.persistence.text.TextGroupRepository;
import es.upm.amusementpark.persistence.text.TextPassRepository;
import es.upm.amusementpark.persistence.text.TextTicketRepository;
import es.upm.amusementpark.persistence.text.TextUserRepository;
import es.upm.amusementpark.presentation.CommandCatalog;
import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.presentation.ExtendedHelpFormatter;
import es.upm.amusementpark.presentation.HelpGenerator;
import es.upm.amusementpark.presentation.SyntaxHelpFormatter;

/**
 * Composition root: creates the repositories for a data directory and injects them, together with
 * the controller collaborations and help strategies, into the singleton controllers and the
 * presentation layer. Normal and --fly modes only differ in the directory passed here.
 */
public final class AppInitializer {

    private AppInitializer() {
    }

    /** Wires the business layer to text repositories stored in {@code dataDir}. */
    public static void initializeBusiness(Path dataDir, PasswordHasher hasher) {
        UserController users = UserController.getInstance();
        users.configure(new TextUserRepository(dataDir.resolve("users.txt")), hasher);

        ParkController park = ParkController.getInstance();
        park.configure(users,
                new TextGroupRepository(dataDir.resolve("groups.txt")),
                new TextTicketRepository(dataDir.resolve("tickets.txt")),
                new TextAttractionRepository(dataDir.resolve("attractions.txt")),
                new TextPassRepository(dataDir.resolve("passes.txt")));

        PassController.getInstance().configure(park);
        InvoiceController.getInstance().configure(park, users);
    }

    /** Wires the whole application and returns the command processor writing to {@code out}. */
    public static CommandProcessor initialize(Path dataDir, PasswordHasher hasher, PrintStream out) {
        initializeBusiness(dataDir, hasher);
        return new CommandProcessor(ParkController.getInstance(), PassController.getInstance(),
                InvoiceController.getInstance(),
                new HelpGenerator(CommandCatalog.COMMANDS, new SyntaxHelpFormatter()),
                new HelpGenerator(CommandCatalog.COMMANDS, new ExtendedHelpFormatter()),
                out);
    }
}
