package es.upm.park.presentation;

import es.upm.park.presentation.help.CommandCatalog;
import es.upm.park.presentation.help.CommandDescription;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpCommandsTest {

    private final CommandProcessor processor = new CommandProcessor();

    @Test
    void extendedHelpShowsSyntaxAndPurposeOfEveryHelpCommand() {
        String basic = processor.process("HELP").output();
        String extended = processor.process("HELP EXTENDED").output();
        List<String> basicLines = List.of(basic.split(System.lineSeparator()));
        for (CommandDescription command : CommandCatalog.standard()) {
            assertTrue(basicLines.contains(command.syntax()), command.syntax());
            assertTrue(extended.contains(command.syntax() + System.lineSeparator() + "    " + command.purpose()),
                    command.syntax());
        }
        assertTrue(basicLines.contains("HELP EXTENDED"));
    }

    @Test
    void catalogAndHandlersAgree() {
        Set<String> catalogKeywords = new TreeSet<>();
        CommandCatalog.standard().forEach(command -> catalogKeywords.add(command.keyword()));
        Set<String> handled = new TreeSet<>(processor.handledCommands());
        assertTrue(catalogKeywords.containsAll(handled), "every handler is documented");
        Set<String> pending = new TreeSet<>(catalogKeywords);
        pending.removeAll(handled);
        // USER UPDATE is mandated in HELP but implemented by a later user milestone.
        assertEquals(Set.of("USER UPDATE"), pending);
    }
}
