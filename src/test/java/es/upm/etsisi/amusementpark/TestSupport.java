package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.PasswordHasher;
import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.persistence.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.persistence.TextRepositories;
import es.upm.etsisi.amusementpark.presentation.CommandCatalog;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import es.upm.etsisi.amusementpark.presentation.ExtendedHelpFormat;
import es.upm.etsisi.amusementpark.presentation.HelpGenerator;
import es.upm.etsisi.amusementpark.presentation.SyntaxHelpFormat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

/** Shared wiring for tests: cheap BCrypt cost and the standard composition. */
final class TestSupport {

    static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    static final PasswordHasher HASHER = new BCryptPasswordHasher(4);

    private TestSupport() {
    }

    static ParkController initialize(Path dir, PrintStream warnings) throws ParkException {
        dir.toFile().mkdirs();
        ParkController controller = ParkController.getInstance();
        controller.initialize(TextRepositories.inDirectory(dir, warnings), HASHER, TODAY);
        InvoiceController.getInstance().configure(controller);
        return controller;
    }

    static ParkController initialize(Path dir) throws ParkException {
        return initialize(dir, new PrintStream(new ByteArrayOutputStream()));
    }

    static CommandProcessor processor(ParkController park, PrintStream out) {
        CommandCatalog catalog = CommandCatalog.standard();
        return new CommandProcessor(park, InvoiceController.getInstance(), Map.of(
                "HELP", new HelpGenerator(catalog, new SyntaxHelpFormat()),
                "HELP EXTENDED", new HelpGenerator(catalog, new ExtendedHelpFormat())), out);
    }
}
