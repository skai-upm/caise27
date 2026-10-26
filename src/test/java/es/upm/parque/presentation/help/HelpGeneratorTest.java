package es.upm.parque.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo("USER DELETE;Correo;Contraseña", "Deletes a user."),
            new CommandInfo("HELP", "Lists commands."));

    @Test
    void commandInfoKnowsOperationAndParameters() {
        assertEquals("USER DELETE", COMMANDS.get(0).operation());
        assertEquals(2, COMMANDS.get(0).parameterCount());
        assertEquals("HELP", COMMANDS.get(1).operation());
        assertEquals(0, COMMANDS.get(1).parameterCount());
    }

    @Test
    void syntaxFormatListsSyntaxOnly() {
        String nl = System.lineSeparator();
        assertEquals("USER DELETE;Correo;Contraseña" + nl + "HELP",
                new HelpGenerator(COMMANDS, new SyntaxHelpFormatter()).generate());
    }

    @Test
    void extendedFormatAddsDescriptions() {
        String nl = System.lineSeparator();
        assertEquals(String.join(nl, "USER DELETE;Correo;Contraseña", "    Deletes a user.", "HELP", "    Lists commands."),
                new HelpGenerator(COMMANDS, new ExtendedHelpFormatter()).generate());
    }

    @Test
    void anyInjectedBehaviourIsUsed() {
        HelpFormatter csv = commands -> String.join(",", commands.stream().map(CommandInfo::operation).toList());
        assertEquals("USER DELETE,HELP", new HelpGenerator(COMMANDS, csv).generate());
    }
}
