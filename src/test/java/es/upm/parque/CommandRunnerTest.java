package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

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
    private ByteArrayOutputStream output;
    private CommandRunner runner;

    @BeforeEach
    void setUp() throws IOException {
        manager = new UserManager(new UserRepository(dir.resolve("users.txt")));
        output = new ByteArrayOutputStream();
        runner = new CommandRunner(new CommandProcessor(manager,
                new GroupManager(new GroupRepository(dir.resolve("groups.txt")), manager), new SystemDate(LocalDate.of(2026, 9, 1))), new PrintStream(output, true, StandardCharsets.UTF_8));
    }

    private List<String> run(String input) throws IOException {
        runner.run(new BufferedReader(new StringReader(input)));
        return output.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void executesCommandsInOrderAndSkipsBlankLines() throws IOException {
        List<String> lines = run(CREATE_ANA + "\n\n   \n" + CREATE_PEDRO + "\nUSER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\n");
        assertEquals(List.of("OK USER CREATED", "OK USER CREATED", "OK USER UPDATED"), lines);
        assertEquals("699999999", manager.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void stopsAtExit() throws IOException {
        List<String> lines = run(CREATE_ANA + "\nEXIT\n" + CREATE_PEDRO + "\n");
        assertEquals(List.of("OK USER CREATED", "OK EXIT"), lines);
        assertEquals(1, manager.getUsers().size());
    }
}
