package es.upm.park.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.repository.UserRepository;
import es.upm.park.service.UserService;

class CommandRunnerTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private UserRepository repo;
    private CommandRunner runner;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        repo = new UserRepository();
        runner = new CommandRunner(new CommandProcessor(new UserService(repo)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private List<String> output() {
        return buffer.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void runsStandardInputUntilExit() {
        runner.runStandardInput(new ByteArrayInputStream((ANA + "\n\nEXIT\nHELP\n").getBytes(StandardCharsets.UTF_8)));
        assertEquals(List.of("User created: 12345678Z", "Bye"), output());
    }

    @Test
    void runsFileInOrder() throws IOException {
        Path file = dir.resolve("in.txt");
        Files.write(file, List.of(ANA, "USER DELETE;ana@correo.com;Clave1!"));
        runner.runFile(file.toString());
        assertEquals(List.of("User created: 12345678Z", "User deleted: ana@correo.com"), output());
        assertEquals(0, repo.findAll().size());
    }

    @Test
    void reportsUnreadableSources() {
        runner.runFile(dir.toString());
        runner.runFile("\0bad");
        runner.runStandardInput(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("closed");
            }
        });
        assertEquals(List.of("Error: cannot read command file " + dir, "Error: cannot read command file \0bad",
                "Error: cannot read commands"), output());
    }
}
