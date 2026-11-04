package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceDirector;
import es.upm.etsisi.amusementpark.business.service.AttractionManager;
import es.upm.etsisi.amusementpark.business.service.GroupManager;
import es.upm.etsisi.amusementpark.business.service.TicketManager;
import es.upm.etsisi.amusementpark.business.service.UserManager;
import es.upm.etsisi.amusementpark.persistence.AttractionFileRepository;
import es.upm.etsisi.amusementpark.persistence.GroupFileRepository;
import es.upm.etsisi.amusementpark.persistence.GroupTicketFileRepository;
import es.upm.etsisi.amusementpark.persistence.TicketFileRepository;
import es.upm.etsisi.amusementpark.persistence.UserFileRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandRunnerTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private UserManager manager;
    private CommandRunner runner;
    private ByteArrayOutputStream bytes;
    private PrintStream out;

    @BeforeEach
    void setUp() throws Exception {
        manager = new UserManager(new UserFileRepository(dir.resolve("users.txt")));
        GroupManager groups = new GroupManager(new GroupFileRepository(dir.resolve("groups.txt")), manager);
        ParkController.shutdown();
        runner = new CommandRunner(new CommandProcessor(ParkController.initialize(manager,
                groups, new TicketManager(new TicketFileRepository(dir.resolve("tickets.txt")),
                        new GroupTicketFileRepository(dir.resolve("grouptickets.txt")), manager, groups),
                new AttractionManager(new AttractionFileRepository(dir.resolve("attractions.txt"))),
                new InvoiceDirector())));
        bytes = new ByteArrayOutputStream();
        out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() {
        ParkController.shutdown();
    }

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void stopsAtExitAndIgnoresLaterCommands() {
        String input = String.join("\n", CREATE_ANA, "EXIT", CREATE_PEDRO);
        assertTrue(runner.run(new BufferedReader(new StringReader(input)), out));
        assertEquals(1, manager.getUsers().size());
        assertTrue(output().contains("OK: Bye"));
        assertFalse(output().contains("pedro"));
    }

    @Test
    void skipsBlankLinesAndEndsAtEndOfInput() {
        String input = CREATE_ANA + "\n\n   \n" + CREATE_PEDRO + "\n";
        assertTrue(runner.run(new BufferedReader(new StringReader(input)), out));
        assertEquals(2, manager.getUsers().size());
        assertEquals(2, output().lines().count());
    }

    @Test
    void readFailureIsReportedNotThrown() {
        Reader failing = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("disk error");
            }

            @Override
            public void close() {
                // nothing to close
            }
        };
        assertFalse(runner.run(new BufferedReader(failing), out));
        assertTrue(output().startsWith("ERROR"));
    }
}
