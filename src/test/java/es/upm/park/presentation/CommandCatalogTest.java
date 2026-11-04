package es.upm.park.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.service.ParkSession;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandCatalogTest {

    @TempDir
    Path dir;

    @BeforeEach
    @AfterEach
    void resetControllers() {
        ParkSession.resetControllers();
    }

    @Test
    void everyRegisteredCommandIsRecognisedOnce() {
        Set<String> syntaxes = new HashSet<>();
        try (ParkSession session = ParkSession.permanent(dir)) {
            CommandProcessor processor = App.newCommandProcessor(session.controller());
            for (CommandInfo c : CommandCatalog.commands()) {
                assertTrue(syntaxes.add(c.syntax()), "duplicate " + c.syntax());
                String operation = c.syntax().split(";")[0];
                String reply = operation.equals("EXIT") ? "" : processor.process(operation + ";x");
                assertFalse(reply.contains("unknown command"), operation + " -> " + reply);
            }
        }
    }
}
