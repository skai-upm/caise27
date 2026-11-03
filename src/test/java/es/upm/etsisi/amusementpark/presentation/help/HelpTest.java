package es.upm.etsisi.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.App;
import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HelpTest {

    static final List<CommandInfo> TWO = List.of(new CommandInfo("A;x", "Does A."), new CommandInfo("B", "Does B."));

    @TempDir
    Path dir;

    @Test
    void syntaxFormatterListsSyntaxOnly() {
        assertEquals(String.join(System.lineSeparator(), "Available commands:", "  A;x", "  B"),
                new HelpGenerator(TWO, new SyntaxHelpFormatter()).generate());
    }

    @Test
    void extendedFormatterAddsDescriptions() {
        assertEquals(String.join(System.lineSeparator(), "Available commands (extended):", "  A;x", "      Does A.",
                "  B", "      Does B."), new HelpGenerator(TWO, new ExtendedHelpFormatter()).generate());
    }

    @Test
    void catalogHasTwentySixDescribedCommands() {
        assertEquals(26, CommandCatalog.COMMANDS.size());
        assertTrue(CommandCatalog.COMMANDS.stream().allMatch(c -> !c.getSyntax().isBlank() && !c.getDescription().isBlank()));
    }

    @Test
    void helpModesUseTheSameCommandsInTheSameOrder() {
        CommandProcessor p = App.createProcessor(dir);
        List<String> basic = p.process("HELP").lines().skip(1).map(String::trim).toList();
        List<String> extended = p.process("HELP EXTENDED").lines().skip(1).toList();
        List<String> syntaxes = extended.stream().filter(l -> l.startsWith("  ") && !l.startsWith("      "))
                .map(String::trim).toList();
        assertEquals(basic, syntaxes);
        assertEquals(CommandCatalog.COMMANDS.stream().map(CommandInfo::getSyntax).toList(), basic);
        assertEquals(2 * CommandCatalog.COMMANDS.size(), extended.size());
        assertTrue(p.process("HELP EXTENDED").contains("  TICKET DELETE;Id" + System.lineSeparator()
                + "      Deletes an individual ticket dated after the system date."));
    }

    @Test
    void everyRegisteredCommandIsRecognised() {
        CommandProcessor p = App.createProcessor(dir);
        for (CommandInfo c : CommandCatalog.COMMANDS) {
            String operation = c.getSyntax().split(";")[0];
            if (!operation.equals("EXIT")) {
                assertFalse(p.process(operation).startsWith("ERROR: Unknown command"), operation);
            }
        }
    }

    @Test
    void helpExtendedRejectsParameters() {
        assertTrue(App.createProcessor(dir).process("HELP EXTENDED;x").startsWith("ERROR:"));
    }

    @Test
    void alternativeFormatterIsInjectedWithoutChangingTheProcessor() {
        App.createProcessor(dir);
        HelpFormatter upper = commands -> "COMMANDS: " + commands.size();
        CommandProcessor p = new CommandProcessor(ParkController.getInstance(),
                new HelpGenerator(CommandCatalog.COMMANDS, upper), new HelpGenerator(TWO, upper));
        assertEquals("COMMANDS: " + CommandCatalog.COMMANDS.size(), p.process("HELP"));
        assertEquals("COMMANDS: 2", p.process("HELP EXTENDED"));
        assertTrue(TestData.processor(ParkController.getInstance()).process("HELP").startsWith("Available commands:"));
    }
}
