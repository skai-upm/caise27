package com.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class HelpGeneratorTest {

    private final HelpGenerator generator = new HelpGenerator(CommandCatalog.commands(),
            Map.of("", new SyntaxHelpFormat(), "EXTENDED", new ExtendedHelpFormat()));

    @Test
    public void conciseHelpListsEverySyntaxWithoutDescriptions() {
        String help = generator.generate("");
        for (CommandInfo command : CommandCatalog.commands()) {
            assertTrue(help.contains(command.syntax()), command.syntax());
            assertFalse(help.contains(command.description()), command.description());
        }
    }

    @Test
    public void extendedHelpListsSyntaxAndDescription() {
        String help = generator.generate("EXTENDED");
        for (CommandInfo command : CommandCatalog.commands()) {
            assertTrue(help.contains(command.syntax() + System.lineSeparator() + "      " + command.description()),
                    command.syntax());
        }
    }

    @Test
    public void injectedFormatIsUsed() {
        HelpFormat count = commands -> "commands: " + commands.size();
        HelpGenerator custom = new HelpGenerator(List.of(new CommandInfo("A", "a"), new CommandInfo("B", "b")),
                Map.of("COUNT", count));
        assertEquals("commands: 2", custom.generate("COUNT"));
        assertTrue(custom.supports("COUNT"));
        assertTrue(custom.generate("").startsWith("ERROR"));
    }

    @Test
    public void catalogCoversAllCommandFamilies() {
        String help = generator.generate("");
        for (String syntax : new String[] {"SPEEDPASS CREATE;IdTicket;Cantidad", "SPEEDPASS DELETE;IdPase",
            "SPECIALPASS CREATE;IdTicket", "SPECIALPASS DELETE;IdPase", "ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo",
            "ATTRACTION ENTER;Id;IdTicket;SPEED", "INVOICE CREATE;USER;Correo", "INVOICE CREATE;GROUP;IdGrupo",
            "HELP EXTENDED"}) {
            assertTrue(help.contains(syntax), syntax);
        }
    }
}
