package es.upm.parque;

import es.upm.parque.presentation.CommandDefinition;
import es.upm.parque.presentation.CommandRegistry;
import es.upm.parque.presentation.help.HelpGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class HelpTest extends ParkTestSupport {

    private static final List<String> DELIVERY_THREE_SYNTAX = List.of(
            "INVOICE CREATE;USER|GROUP;", "HELP EXTENDED", "SPEEDPASS CREATE;IdEntrada;Cantidad",
            "SPEEDPASS DELETE;IdPase", "SPECIALPASS CREATE;IdEntrada", "SPECIALPASS DELETE;IdPase",
            "ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad;Tipo", "ATTRACTION ENTER;IdAtraccion;IdEntrada;SPEED");

    @Test
    public void helpListsDeliveryThreeCommands() {
        String help = run("HELP");
        for (String syntax : DELIVERY_THREE_SYNTAX) {
            assertTrue(help.contains(syntax), syntax + " missing from HELP");
        }
    }

    @Test
    public void extendedHelpShowsSyntaxAndDescriptionOfEveryCommand() {
        String concise = run("HELP");
        String extended = run("HELP EXTENDED");
        int listed = 0;
        for (String line : concise.split("\\R")) {
            if (line.startsWith("  ")) {
                String syntax = line.trim();
                int at = extended.indexOf("  " + syntax + System.lineSeparator());
                assertTrue(at >= 0, syntax + " missing from HELP EXTENDED");
                String description = extended.substring(at).split("\\R")[1];
                assertTrue(description.startsWith("      ") && description.trim().length() > 10,
                        "no description for " + syntax);
                listed++;
            }
        }
        assertEquals(28, listed, "HELP lists " + listed + " commands");
        assertFalse(concise.contains("Lists the syntax of every command"), "HELP stays concise");
    }

    @Test
    public void helpBehaviourIsInjected() {
        CommandRegistry registry = new CommandRegistry();
        registry.register("FOO;X", "Does foo.", p -> "OK");
        HelpGenerator generator = new HelpGenerator(commands -> commands.stream()
                .map(CommandDefinition::name).reduce("", String::concat));
        assertEquals("FOO", generator.generate(registry));
    }

    @Test
    public void optionalParametersAreRegisteredAsVariants() {
        assertTrue(run("ATTRACTION CREATE;A01;Tornado;20").startsWith("OK"));
        assertTrue(run("ATTRACTION CREATE;A02;Rio;20;GROUP").startsWith("OK"));
        String wrong = run("ATTRACTION CREATE;A03");
        assertTrue(wrong.contains("ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad | ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad;Tipo"), wrong);
    }
}
