package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import es.upm.parque.TestContext;
import es.upm.parque.presentation.command.Command;
import es.upm.parque.presentation.command.CommandRegistry;
import es.upm.parque.presentation.command.SimpleCommand;
import es.upm.parque.presentation.help.ExtendedHelpFormatter;
import es.upm.parque.presentation.help.HelpGenerator;
import es.upm.parque.presentation.help.SyntaxHelpFormatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class HelpTest {

    @TempDir
    Path dir;

    @Test
    void helpShowsSyntaxAndExtendedHelpAddsDescriptions() throws IOException {
        TestContext ctx = TestContext.start(dir);
        String help = ctx.run("HELP");
        String extended = ctx.run("HELP EXTENDED");
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(!help.contains("Registers a user"));
        assertTrue(extended.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(extended.contains("Registers a user"));
        assertTrue(extended.contains("Shows the syntax and a description of every command."));
    }

    @Test
    void bothHelpsListEveryRegisteredCommand() throws IOException {
        TestContext ctx = TestContext.start(dir);
        CommandRegistry registry = new CommandRegistry();
        CommandProcessor processor = new CommandProcessor(registry);
        registry.register(new SimpleCommand("PING", "PING;Texto", "Answers with the same text.", p -> "OK " + p[0]));
        HelpGenerator help = new HelpGenerator(registry, new SyntaxHelpFormatter());
        HelpGenerator extended = new HelpGenerator(registry, new ExtendedHelpFormatter());
        assertTrue(help.generate().contains("PING;Texto"));
        assertTrue(extended.generate().contains("Answers with the same text."));
        assertEquals("OK hola", processor.process("PING;hola"));
        String fullHelp = ctx.run("HELP");
        for (String keyword : new String[] {"USER CREATE", "GROUPTICKET DELETE", "SPECIALPASS DELETE",
                "INVOICE CREATE", "HELP EXTENDED", "EXIT"}) {
            assertTrue(fullHelp.contains(keyword), keyword);
        }
    }

    @Test
    void helpFormatIsInjected() {
        CommandRegistry registry = new CommandRegistry();
        Command command = new SimpleCommand("PING", "PING", "Pings.", p -> "OK");
        registry.register(command);
        HelpGenerator custom = new HelpGenerator(registry, commands -> "count=" + commands.size());
        assertEquals("count=1", custom.generate());
    }
}
