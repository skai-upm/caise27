package es.upm.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.upm.amusementpark.persistence.ParkStorage;
import es.upm.amusementpark.service.Park;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandRunnerTest {

    @TempDir
    Path dir;

    private ByteArrayOutputStream output;
    private Park park;
    private CommandRunner runner;

    @BeforeEach
    void setUp() throws Exception {
        output = new ByteArrayOutputStream();
        park = Park.getInstance();
        park.initialize(ParkStorage.persistent(dir, new PrintStream(OutputStream.nullOutputStream())),
                LocalDate.of(2027, 4, 15));
        runner = new CommandRunner(new CommandProcessor(park, new PrintStream(output, true)));
    }

    private void run(String... lines) throws Exception {
        runner.run(new BufferedReader(new StringReader(String.join("\n", lines))));
    }

    @Test
    void processesInOrderIgnoringBlankLines() throws Exception {
        run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "   ",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");

        assertEquals(2, park.getUsers().size());
        assertEquals("699999999", park.findUser("ana@correo.com").getPhone());
        assertEquals(String.join(System.lineSeparator(), "USER CREATED", "USER CREATED", "USER UPDATED", ""),
                output.toString());
    }

    @Test
    void stopsAfterExit() throws Exception {
        run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "EXIT",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");

        assertNotNull(park.findUser("ana@correo.com"));
        assertNull(park.findUser("pedro@correo.com"));
    }

    @Test
    void continuesAfterInvalidCommands() throws Exception {
        run("NONSENSE", "USER CREATE;Ana",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");

        assertNotNull(park.findUser("ana@correo.com"));
    }
}
