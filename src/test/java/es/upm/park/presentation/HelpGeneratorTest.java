package es.upm.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelpGeneratorTest {

    private static final List<String> PREVIOUS_HELP = List.of(
            "Available commands:",
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "GROUP CREATE;Id;Nombre",
            "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo",
            "GROUP DELETE;Id",
            "TICKET CREATE;Id;Correo;yyyy-MM-dd",
            "TICKET DELETE;Id",
            "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
            "GROUPTICKET DELETE;Id",
            "SPEEDPASS CREATE;IdCompra;IdTicket;Cantidad",
            "SPEEDPASS DELETE;IdPase",
            "SPECIALPASS CREATE;Id;IdTicket",
            "SPECIALPASS DELETE;IdPase",
            "ATTRACTION CREATE;Id;Nombre;Capacidad",
            "ATTRACTION CREATE;Id;Nombre;Capacidad;Tipo",
            "ATTRACTION ENTER;IdAtraccion;IdTicket",
            "ATTRACTION ENTER;IdAtraccion;IdTicket;SPEED",
            "ATTRACTION EXIT;IdAtraccion;IdTicket",
            "ATTRACTION STATUS;IdAtraccion",
            "INVOICE CREATE;USER;Correo",
            "INVOICE CREATE;GROUP;IdGrupo",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    private final CommandCatalog catalog = CommandCatalog.standard();

    @Test
    void conciseHelpKeepsPreviousLinesAndAddsHelpExtended() {
        List<String> lines = new HelpGenerator(catalog, new SyntaxHelpFormatter()).generate().lines().toList();
        List<String> expected = new java.util.ArrayList<>(PREVIOUS_HELP);
        expected.add(expected.indexOf("EXIT"), "HELP EXTENDED");
        assertEquals(expected, lines);
    }

    @Test
    void extendedHelpHasDescriptionForEveryCommand() {
        List<String> lines = new HelpGenerator(catalog, new ExtendedHelpFormatter()).generate().lines().toList();
        assertEquals("Available commands (extended):", lines.get(0));
        assertEquals(1 + 2 * catalog.commands().size(), lines.size());
        for (int i = 0; i < catalog.commands().size(); i++) {
            CommandInfo c = catalog.commands().get(i);
            assertEquals(c.syntax(), lines.get(1 + 2 * i));
            assertTrue(lines.get(2 + 2 * i).startsWith("    "));
            assertFalse(lines.get(2 + 2 * i).isBlank());
        }
    }

    @Test
    void injectedFormatterIsUsed() {
        HelpFormatter countOnly = commands -> commands.size() + " commands";
        assertEquals(catalog.commands().size() + " commands", new HelpGenerator(catalog, countOnly).generate());
    }

    @Test
    void catalogIsTheSingleSourceForBothModes() {
        CommandCatalog custom = new CommandCatalog(List.of(new CommandInfo("PING", "Answers pong.")));
        assertTrue(new HelpGenerator(custom, new SyntaxHelpFormatter()).generate().contains("PING"));
        assertTrue(new HelpGenerator(custom, new ExtendedHelpFormatter()).generate().contains("Answers pong."));
    }
}
