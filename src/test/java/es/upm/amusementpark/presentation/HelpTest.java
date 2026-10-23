package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.controller.Park;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HelpTest {

    private static final String NL = System.lineSeparator();

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = TestParks.open(dir);
        processor = new CommandProcessor(park);
    }

    @Test
    void helpListsEverySyntaxOnItsOwnLine() {
        String help = processor.process("HELP").output();
        List<CommandInfo> infos = processor.commandInfos();
        String[] lines = help.split(NL);
        assertEquals("Available commands:", lines[0]);
        assertEquals(infos.size(), lines.length - 1);
        for (int i = 0; i < infos.size(); i++) {
            assertEquals("  " + infos.get(i).syntax(), lines[i + 1]);
        }
        for (String required : List.of("INVOICE CREATE;USER;<email>", "INVOICE CREATE;GROUP;<groupIdentifier>",
                "HELP", "HELP EXTENDED", "EXIT", "SYSTEM DATE;<yyyy-MM-dd>", "ATTRACTION STATUS;<attraction>")) {
            assertTrue(help.contains(NL + "  " + required), required);
        }
    }

    @Test
    void extendedHelpDescribesEachCommand() {
        String help = processor.process("help extended").output();
        String[] lines = help.split(NL);
        assertEquals("Available commands (extended):", lines[0]);
        List<CommandInfo> infos = processor.commandInfos();
        assertEquals(2 * infos.size(), lines.length - 1);
        for (int i = 0; i < infos.size(); i++) {
            assertEquals("  " + infos.get(i).syntax(), lines[1 + 2 * i]);
            assertTrue(lines[2 + 2 * i].startsWith("      ") && lines[2 + 2 * i].trim().length() > 10,
                    infos.get(i).syntax());
            assertFalse(infos.get(i).description().isBlank());
        }
    }

    @Test
    void helpStyleIsInjected() {
        HelpFormatter countOnly = commands -> commands.size() + " commands";
        CommandProcessor custom = new CommandProcessor(park, new HelpGenerator(countOnly),
                new HelpGenerator(new SyntaxHelpFormatter()));
        assertEquals(processor.commandInfos().size() + " commands", custom.process("HELP").output());
        assertTrue(custom.process("HELP EXTENDED").output().startsWith("Available commands:"));
        assertEquals("USER CREATED", custom.process(CommandProcessorTest.CREATE_ANA).output());
    }

    @Test
    void helpCommandsTakeNoArguments() {
        assertTrue(processor.process("HELP EXTENDED;x").output().startsWith("ERROR"));
        assertTrue(processor.process("HELP;EXTENDED").output().startsWith("ERROR"));
    }
}
