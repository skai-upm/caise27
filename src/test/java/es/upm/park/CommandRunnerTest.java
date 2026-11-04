package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class CommandRunnerTest {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;
    private UserManager manager;
    private CommandRunner runner;

    @BeforeEach
    void setUp() throws IOException {
        manager = new UserManager(new UserFileStore(dir.resolve("users.txt")));
        GroupManager groups = new GroupManager(new GroupFileStore(dir.resolve("groups.txt")), manager);
        runner = new CommandRunner(new CommandProcessor(manager, groups));
    }

    private List<String> run(String... lines) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        runner.run(new BufferedReader(new StringReader(String.join("\n", lines))), out);
        String text = bytes.toString(StandardCharsets.UTF_8);
        return text.isEmpty() ? List.of() : text.lines().toList();
    }

    @Test
    void processesCommandsInOrderUntilEndOfInput() throws IOException {
        List<String> out = run(CREATE_ANA, CREATE_PEDRO, "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        assertEquals(3, out.size());
        assertTrue(out.stream().allMatch(l -> l.startsWith("OK: ")));
        assertEquals("699999999", manager.getUsers().get(0).getPhone());
    }

    @Test
    void blankLinesAreSkipped() throws IOException {
        List<String> out = run(CREATE_ANA, "", "   ", "\t", "EXIT");
        assertEquals(List.of("OK: User ana@correo.com created.", "OK: Bye."), out);
    }

    @Test
    void nothingRunsAfterExit() throws IOException {
        List<String> out = run(CREATE_ANA, "EXIT", CREATE_PEDRO, "HELP");
        assertEquals(2, out.size());
        assertEquals(1, manager.getUsers().size());
        assertEquals("12345678Z", manager.getUsers().get(0).getDni());
    }

    @Test
    void emptyInputProducesNoOutput() throws IOException {
        assertTrue(run().isEmpty());
    }

    @Test
    void errorsDoNotStopProcessing() throws IOException {
        List<String> out = run("FOO", CREATE_ANA);
        assertTrue(out.get(0).startsWith("ERROR: "));
        assertEquals(1, manager.getUsers().size());
    }
}
