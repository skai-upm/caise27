package es.upm.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.util.List;
import org.junit.jupiter.api.Test;

class HelpTest {

    private final CommandProcessor p = ApplicationInitializer.initialize(Repositories.inMemory());

    @Test
    void extendedHelpShowsSyntaxAndPurposeForEveryCommand() {
        String ext = p.process("HELP EXTENDED");
        for (CommandInfo c : p.catalogue()) {
            assertFalse(c.purpose().isEmpty(), c.syntax());
            assertTrue(ext.contains(c.syntax()), c.syntax());
            assertTrue(ext.contains(c.purpose()), c.purpose());
        }
        assertTrue(ext.contains("Registers a new user"));
    }

    @Test
    void helpUnchangedAndExtraElementsRejected() {
        String help = p.process("HELP");
        assertTrue(help.startsWith("Available commands:"));
        assertFalse(help.contains("Registers a new user"));
        assertTrue(help.contains("HELP EXTENDED"));
        assertTrue(p.process("HELP EXTENDED;x").startsWith("ERROR"));
    }

    @Test
    void newVariantThroughInjectedBehavior() {
        HelpGenerator compact = new HelpGenerator(cmds -> String.valueOf(cmds.size()) + " commands");
        assertEquals(p.catalogue().size() + " commands", compact.generate(p.catalogue()));
        assertEquals("Available commands:" + System.lineSeparator() + "  X",
                new HelpGenerator(new SyntaxHelpFormat()).generate(List.of(new CommandInfo("X", "", "p"))));
    }
}
