package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path tempDir;

    private UserManager manager;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        Park park = new Park(new StorageConfig(tempDir));
        manager = park.getUserManager();
        processor = new CommandProcessor(park);
    }

    @Test
    void createsUserFromCommand() {
        assertTrue(processor.execute(CREATE_ANA).output().startsWith("OK:"));
        assertEquals("Garcia Lopez", manager.findByEmail("ana@correo.com").getSurname());
    }

    @Test
    void rejectsInsufficientParameters() {
        assertTrue(processor.execute("USER CREATE;Ana;Garcia Lopez;12345678Z").output().startsWith("ERROR:"));
        assertTrue(processor.execute("USER DELETE;ana@correo.com").output().startsWith("ERROR:"));
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;NAME").output().startsWith("ERROR:"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void rejectsEmptyAndExtraParameters() {
        assertTrue(processor.execute(
                "USER CREATE;;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456").output()
                .startsWith("ERROR:"));
        assertTrue(processor.execute(CREATE_ANA + ";extra").output().startsWith("ERROR:"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void rejectsUnknownCommands() {
        assertTrue(processor.execute("USER LIST").output().startsWith("ERROR:"));
        assertTrue(processor.execute("hello").output().startsWith("ERROR:"));
        assertTrue(processor.execute(";;;").output().startsWith("ERROR:"));
    }

    @Test
    void reportsValidationErrors() {
        assertTrue(processor.execute(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456").output()
                .startsWith("ERROR:"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void deletesWithCredentials() {
        processor.execute(CREATE_ANA).output();
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Wrong1!").output().startsWith("ERROR:"));
        assertTrue(processor.execute("USER DELETE;ana@correo.com;Clave1!").output().startsWith("OK:"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void chainedUpdatesFromSource() {
        processor.execute(CREATE_ANA).output();
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").output().startsWith("OK:"));
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").output()
                .startsWith("OK:"));
        assertTrue(processor.execute("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#").output()
                .startsWith("OK:"));
        assertTrue(processor.execute("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666").output()
                .startsWith("OK:"));
        User user = manager.findByEmail("ana.nueva@correo.com");
        assertEquals("Ana Maria", user.getName());
        assertEquals("9999888877776666", user.getCard());
    }

    @Test
    void dniAndUnknownFieldsAreNotUpdatable() {
        processor.execute(CREATE_ANA).output();
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").output().startsWith("ERROR:"));
        assertTrue(processor.execute("USER UPDATE;ana@correo.com;Clave1!;AGE;30").output().startsWith("ERROR:"));
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
    }

    private String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        processor.run(new BufferedReader(new StringReader(input)), new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void helpListsEveryCommandSyntax() {
        CommandResult result = processor.execute("HELP");
        assertFalse(result.exit());
        for (String syntax : List.of(
                "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "USER DELETE;Correo;Contraseña",
                "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                "SYSTEM DATE;yyyy-MM-dd",
                "SYSTEM PRICE;Precio",
                "GROUP CREATE;IdGrupo;Nombre",
                "GROUP ADD;IdGrupo;Correo",
                "GROUP REMOVE;IdGrupo;Correo",
                "GROUP DELETE;IdGrupo",
                "TICKET CREATE;IdTicket;Correo;yyyy-MM-dd",
                "TICKET DELETE;IdTicket",
                "GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd",
                "GROUPTICKET DELETE;IdTicket",
                "ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad",
                "ATTRACTION ENTER;IdAtraccion;IdTicket",
                "ATTRACTION EXIT;IdAtraccion;IdTicket",
                "ATTRACTION STATUS;IdAtraccion",
                "HELP",
                "EXIT")) {
            assertTrue(result.output().contains(syntax), syntax);
        }
    }

    @Test
    void exitSignalsStop() {
        assertTrue(processor.execute("EXIT").exit());
        assertTrue(processor.execute("  EXIT  ").exit());
    }

    @Test
    void runProcessesInOrderAndSkipsBlankLines() throws IOException {
        String output = run(CREATE_ANA + "\n\n   \nUSER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nHELP\n");
        assertEquals("699999999", manager.findByEmail("ana@correo.com").getPhone());
        assertFalse(output.contains("unknown command"));
    }

    @Test
    void runStopsAtExit() throws IOException {
        run(CREATE_ANA + "\nEXIT\n"
                + "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n");
        assertNotNull(manager.findByEmail("ana@correo.com"));
        assertNull(manager.findByEmail("pedro@correo.com"));
    }

    @Test
    void runRecoversFromInvalidCommands() throws IOException {
        String output = run("garbage\nUSER CREATE;Ana\n" + CREATE_ANA + "\n");
        assertTrue(output.contains("ERROR:"));
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }
}
