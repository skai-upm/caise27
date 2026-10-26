package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private UserManager manager;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        manager = new UserManager(new UserStorage(dir.resolve("users.txt")));
        processor = new CommandProcessor(manager);
    }

    @Test
    void createDeleteAndUpdateCommands() {
        assertTrue(processor.execute(CREATE_ANA).startsWith("OK"));
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK"));
        assertEquals("Ana Maria", manager.getUsers().iterator().next().getName());
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com")
            .startsWith("OK"));
        assertTrue(processor.execute("USER DELETE;ana.nueva@correo.com;Clave1!").startsWith("OK"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void duplicateCreationIsReported() {
        processor.execute(CREATE_ANA);
        String response = processor.execute(
            "USER CREATE;Otra;X;12345678Z;otro@correo.com;Clave1!;600000000;1234567890123456");
        assertTrue(response.startsWith("ERROR") && response.contains("DNI"), response);
    }

    @Test
    void unknownCommandsAreReported() {
        assertTrue(processor.execute("USER LIST").startsWith("ERROR: Unknown command"));
        assertTrue(processor.execute("HELLO").startsWith("ERROR: Unknown command"));
        assertTrue(processor.execute("USER;CREATE").startsWith("ERROR"));
    }

    @Test
    void wrongParameterCountIsReported() {
        assertTrue(processor.execute("USER CREATE;Ana;Garcia").startsWith("ERROR: Expected 7"));
        assertTrue(processor.execute("USER DELETE;ana@correo.com").startsWith("ERROR: Expected 2"));
        assertTrue(processor.execute(CREATE_ANA + ";extra").startsWith("ERROR: Expected 7"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void dniIsNotEditable() {
        processor.execute(CREATE_ANA);
        String response = processor.execute("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertTrue(response.startsWith("ERROR") && response.contains("cannot be updated"), response);
    }

    @Test
    void blankLinesAreIgnored() {
        assertNull(processor.execute("   "));
        assertNull(processor.execute(""));
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.execute("HELP");
        for (String usage : new String[] {"USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor", "HELP", "EXIT"}) {
            assertTrue(help.contains(usage), usage);
        }
        assertFalse(processor.isExitRequested());
    }

    @Test
    void generalCommandsRejectParameters() {
        assertTrue(processor.execute("HELP;extra").startsWith("ERROR: Expected 0"));
        assertTrue(processor.execute("EXIT;now").startsWith("ERROR: Expected 0"));
        assertFalse(processor.isExitRequested());
    }

    @Test
    void exitSetsTheExitFlag() {
        assertTrue(processor.execute("EXIT").startsWith("OK"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void runStopsAtExitAndIgnoresBlankLines() throws IOException {
        String input = CREATE_ANA + "\n\n   \nHELP\nEXIT\n"
            + "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        processor.run(new BufferedReader(new StringReader(input)), new PrintStream(bytes, true, StandardCharsets.UTF_8));

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertFalse(output.contains("ERROR"), output);
        assertTrue(output.contains("Available commands"));
        assertTrue(output.trim().endsWith("OK: Bye"), output);
        assertEquals(1, manager.getUsers().size());
    }
}
