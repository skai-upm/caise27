package es.upm.parque;

import es.upm.parque.business.controller.UserController;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.ConsoleRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the execution modes of the application.
 */
public class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path tempDir;

    private UserController manager;
    private CommandProcessor processor;
    private ByteArrayOutputStream buffer;
    private PrintStream out;

    @BeforeEach
    public void setUp() {
        processor = ParkApplication.start(tempDir, ParkTestSupport.FAST_HASHER);
        manager = UserController.getInstance();
        buffer = new ByteArrayOutputStream();
        out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private void runText(String text, boolean echo) {
        ConsoleRunner.run(new BufferedReader(new StringReader(text)), processor, out, echo);
    }

    @Test
    public void interactiveInputIsProcessedUntilExit() {
        runText(CREATE_ANA + "\nUSER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nHELP\nEXIT\n" + CREATE_PEDRO + "\n", false);
        assertEquals("699999999", manager.findByEmail("ana@correo.com").getPhone());
        assertNull(manager.findByEmail("pedro@correo.com"));
        assertTrue(output().contains("USER DELETE;Correo;Contraseña"));
        assertTrue(output().contains("OK EXIT"));
    }

    @Test
    public void endOfInputWithoutExitStopsNormally() {
        runText(CREATE_ANA + "\n", false);
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    public void blankLinesAreIgnored() {
        runText(CREATE_ANA + "\n\n   \nHELP\nEXIT\n", true);
        assertFalse(output().contains("ERROR"), output());
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    public void fileAndInteractiveModesGiveSameResults() throws Exception {
        Path commands = tempDir.resolve("input_basico.txt");
        Files.write(commands, List.of(CREATE_ANA, CREATE_PEDRO, "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "EXIT", "USER DELETE;pedro@correo.com;Otra2#"));
        try (BufferedReader reader = Files.newBufferedReader(commands)) {
            ConsoleRunner.run(reader, processor, out, true);
        }
        assertEquals("699999999", manager.findByEmail("ana@correo.com").getPhone());
        assertNotNull(manager.findByEmail("pedro@correo.com"), "commands after EXIT must not run");
        assertTrue(output().contains("> " + CREATE_ANA));
        int usersFromFile = manager.getUsers().size();

        CommandProcessor interactive = ParkApplication.start(tempDir.resolve("other"), ParkTestSupport.FAST_HASHER);
        ConsoleRunner.run(new BufferedReader(new StringReader(String.join("\n", Files.readAllLines(commands)))),
                interactive, new PrintStream(new ByteArrayOutputStream()), false);
        assertEquals(usersFromFile, UserController.getInstance().getUsers().size());
        assertEquals("699999999", UserController.getInstance().findByEmail("ana@correo.com").getPhone());
    }

    @Test
    public void nonexistentFileIsReportedWithoutStackTrace() {
        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            App.main(new String[] {tempDir.resolve("no_existe.txt").toString()});
        } finally {
            System.setOut(original);
        }
        String text = captured.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("ERROR: cannot read command file"), text);
        assertTrue(text.contains("no_existe.txt"), text);
        assertFalse(text.contains("Exception"), text);
    }
}
