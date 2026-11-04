package es.upm.etsisi.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private final HelpGenerator generator =
            new HelpGenerator(CommandCatalog.standard(), new SyntaxHelpFormatter(), new ExtendedHelpFormatter());

    @Test
    void conciseHelpKeepsThePreviousTextPlusHelpExtended() {
        List<String> expected = List.of(
                "OK: Available commands:",
                "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "  USER DELETE;Correo;Contraseña",
                "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
                "  GROUP CREATE;Id;Nombre",
                "  GROUP ADD;Id;Correo",
                "  GROUP REMOVE;Id;Correo",
                "  GROUP DELETE;Id",
                "  TICKET CREATE;Id;Correo;yyyy-MM-dd",
                "  TICKET DELETE;Id",
                "  GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
                "  GROUPTICKET DELETE;Id",
                "  ATTRACTION CREATE;Id;Nombre;Capacidad",
                "  ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo",
                "  ATTRACTION ENTER;IdAtraccion;IdTicket",
                "  ATTRACTION EXIT;IdAtraccion;IdTicket",
                "  ATTRACTION STATUS;IdAtraccion",
                "  INVOICE CREATE;USER;Correo",
                "  INVOICE CREATE;GROUP;IdGrupo",
                "  SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad",
                "  SPEEDPASS DELETE;IdPase",
                "  SPECIALPASS CREATE;IdPase;IdTicket",
                "  SPECIALPASS DELETE;IdPase",
                "  SYSTEM DATE;yyyy-MM-dd",
                "  SYSTEM PRICE;Precio",
                "  HELP",
                "  HELP EXTENDED",
                "  EXIT");
        assertEquals(String.join(System.lineSeparator(), expected), generator.concise());
    }

    @Test
    void extendedHelpShowsSyntaxAndDescriptionForEveryCommand() {
        List<String> lines = generator.extended().lines().toList();
        assertEquals("OK: Available commands (extended):", lines.get(0));
        List<CommandInfo> commands = CommandCatalog.standard().commands();
        assertEquals(1 + 2 * commands.size(), lines.size());
        for (int i = 0; i < commands.size(); i++) {
            assertEquals("  " + commands.get(i).syntax(), lines.get(1 + 2 * i));
            assertEquals("      " + commands.get(i).description(), lines.get(2 + 2 * i));
            assertFalse(commands.get(i).description().isBlank());
        }
    }

    @Test
    void injectedFormattingBehaviourIsUsed() {
        HelpFormatter names = cmds -> String.join(",", cmds.stream().map(CommandInfo::operation).distinct().toList());
        HelpGenerator custom = new HelpGenerator(new CommandCatalog(List.of(
                new CommandInfo("HELP", "HELP", "h"), new CommandInfo("EXIT", "EXIT", "e"))), names, names);
        assertEquals("HELP,EXIT", custom.concise());
        assertEquals("HELP,EXIT", custom.extended());
    }

    @Test
    void catalogIsImmutable() {
        assertTrue(CommandCatalog.standard().operations().contains("HELP EXTENDED"));
        List<CommandInfo> commands = CommandCatalog.standard().commands();
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> commands.clear());
    }
}
