package es.upm.parque.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private final CommandCatalog catalog = new CommandCatalog()
            .register("USER DELETE;Correo;Contraseña", "Deletes a user.")
            .register("EXIT", "Ends the session.");

    @Test
    void conciseFormatShowsOnlySyntax() {
        String text = new HelpGenerator(catalog, new ConciseHelpFormat()).generate();
        assertEquals(String.join(System.lineSeparator(), "Available commands:", "  USER DELETE;Correo;Contraseña",
                "  EXIT"), text);
    }

    @Test
    void extendedFormatAddsDescriptions() {
        String text = new HelpGenerator(catalog, new ExtendedHelpFormat()).generate();
        assertTrue(text.contains("  EXIT" + System.lineSeparator() + "      Ends the session."), text);
        assertTrue(text.contains("Deletes a user."), text);
    }

    @Test
    void anyInjectedFormatIsUsed() {
        HelpFormat csv = commands -> commands.stream().map(CommandDescriptor::getSyntax)
                .collect(Collectors.joining(","));
        assertEquals("USER DELETE;Correo;Contraseña,EXIT", new HelpGenerator(catalog, csv).generate());
    }

    @Test
    void newlyRegisteredCommandsAppearInBothModes() {
        catalog.register("NEW CMD;x", "Does something new.");
        assertTrue(new HelpGenerator(catalog, new ConciseHelpFormat()).generate().contains("NEW CMD;x"));
        assertTrue(new HelpGenerator(catalog, new ExtendedHelpFormat()).generate().contains("Does something new."));
    }
}
