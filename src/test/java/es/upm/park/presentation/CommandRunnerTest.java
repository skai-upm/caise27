package es.upm.park.presentation;

import es.upm.park.business.service.ParkSession;
import es.upm.park.business.service.InvoiceDirector;
import es.upm.park.business.service.AttractionManager;
import es.upm.park.business.service.GroupManager;
import es.upm.park.business.service.ParkController;
import es.upm.park.business.service.TicketManager;
import es.upm.park.business.service.UserManager;
import es.upm.park.persistence.AttractionFileStorage;
import es.upm.park.persistence.GroupFileStorage;
import es.upm.park.persistence.GroupTicketFileStorage;
import es.upm.park.persistence.TicketFileStorage;
import es.upm.park.persistence.UserFileStorage;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandRunnerTest {

    static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;
    UserManager manager;
    CommandRunner runner;

    @BeforeEach
    @AfterEach
    void resetControllers() {
        ParkSession.resetControllers();
    }

    @BeforeEach
    void setUp() {
        manager = UserManager.initialize(new UserFileStorage(dir.resolve("users.txt")));
        GroupManager groups = GroupManager.initialize(new GroupFileStorage(dir.resolve("groups.txt")), manager);
        runner = new CommandRunner(new CommandProcessor(ParkController.initialize(manager, groups,
                TicketManager.initialize(new TicketFileStorage(dir.resolve("tickets.txt")),
                        new GroupTicketFileStorage(dir.resolve("group-tickets.txt")), manager, groups),
                AttractionManager.initialize(new AttractionFileStorage(dir.resolve("attractions.txt"))),
                new InvoiceDirector())));
    }

    private String[] run(String input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedReader reader = new BufferedReader(new StringReader(input));
        runner.run(reader, new PrintStream(out, true, StandardCharsets.UTF_8));
        String text = out.toString(StandardCharsets.UTF_8);
        return text.isEmpty() ? new String[0] : text.split("\\R");
    }

    @Test
    void blankLinesAreSkipped() throws IOException {
        String[] out = run(CommandProcessorTest.CREATE_ANA + "\n\n   \nUSER DELETE;ana@correo.com;Clave1!\n");
        assertEquals(2, out.length);
        assertTrue(out[0].startsWith("OK") && out[1].startsWith("OK"));
    }

    @Test
    void nothingRunsAfterExit() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(
                CommandProcessorTest.CREATE_ANA + "\nEXIT\n" + CREATE_PEDRO + "\n"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        runner.run(reader, new PrintStream(out, true, StandardCharsets.UTF_8));
        String[] lines = out.toString(StandardCharsets.UTF_8).split("\\R");
        assertEquals(2, lines.length);
        assertEquals("OK: application terminated", lines[1]);
        assertNotNull(manager.findByEmail("ana@correo.com"));
        assertNull(manager.findByEmail("pedro@correo.com"));
        assertEquals(CREATE_PEDRO, reader.readLine(), "input after EXIT must not be consumed");
    }

    @Test
    void endOfInputWithoutExitTerminates() throws IOException {
        assertEquals(1, run(CommandProcessorTest.CREATE_ANA).length);
        assertEquals(0, run("").length);
    }

    @Test
    void commandsRunInOrder() throws IOException {
        String[] out = run(CommandProcessorTest.CREATE_ANA + "\n"
                + "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com\n"
                + "USER DELETE;ana@correo.com;Clave1!\n"
                + "USER DELETE;ana.nueva@correo.com;Clave1!\n");
        assertTrue(out[2].startsWith("ERROR"));
        assertTrue(out[3].startsWith("OK"));
    }
}
