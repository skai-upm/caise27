package es.upm.etsisi.amusementpark.command;

import static es.upm.etsisi.amusementpark.command.CommandProcessorTest.CREATE_ANA;
import static es.upm.etsisi.amusementpark.command.CommandProcessorTest.CREATE_PEDRO;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.amusementpark.persistence.UserStorage;
import es.upm.etsisi.amusementpark.user.UserManager;

class CommandRunnerTest {

    private static final String UPDATE_PHONE = "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999";

    @TempDir
    Path dir;

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);

    private UserManager users(String name) throws IOException {
        return new UserManager(new UserStorage(dir.resolve(name)));
    }

    private CommandRunner runner(UserManager users) {
        return new CommandRunner(new CommandProcessor(users), out);
    }

    private String output() {
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private Path commandFile(String... lines) throws IOException {
        Path file = dir.resolve("input.txt");
        Files.write(file, List.of(lines), StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void processesInteractiveCommandsInOrder() throws IOException {
        UserManager users = users("users.txt");
        runner(users).run(new BufferedReader(new StringReader(CREATE_ANA + "\n" + UPDATE_PHONE + "\n")));
        assertEquals(List.of("USER CREATED", "USER UPDATED"), output().lines().toList());
        assertEquals("699999999", users.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void processesFileInOrderIgnoringBlankLines() throws IOException {
        UserManager users = users("users.txt");
        assertTrue(runner(users).runFile(commandFile(CREATE_ANA, "", "   ", UPDATE_PHONE)));
        assertEquals(List.of("USER CREATED", "USER UPDATED"), output().lines().toList());
        assertEquals("699999999", users.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void interactiveAndFileModesAreEquivalent() throws IOException {
        String[] sequence = {CREATE_ANA, CREATE_PEDRO, "USER CREATE;bad", UPDATE_PHONE,
            "USER DELETE;pedro@correo.com;Otra2#", "HELP", "EXIT"};

        runner(users("interactive.txt")).run(new BufferedReader(new StringReader(String.join("\n", sequence))));
        String interactiveOutput = output();
        bytes.reset();
        runner(users("file.txt")).runFile(commandFile(sequence));

        assertEquals(interactiveOutput, output());
        assertEquals(Files.readAllLines(dir.resolve("interactive.txt")), Files.readAllLines(dir.resolve("file.txt")));
    }

    @Test
    void missingFileIsReportedInControlledWay() throws IOException {
        assertFalse(runner(users("users.txt")).runFile(dir.resolve("does-not-exist.txt")));
        assertTrue(output().startsWith("ERROR: cannot read command file"));
    }

    @Test
    void directoryIsNotAReadableCommandFile() throws IOException {
        assertFalse(runner(users("users.txt")).runFile(dir));
        assertTrue(output().startsWith("ERROR:"));
    }

    @Test
    void helpListsCommandSyntax() throws IOException {
        runner(users("users.txt")).run(new BufferedReader(new StringReader("HELP\n")));
        String help = output();
        assertTrue(help.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(help.contains("USER DELETE;<email>;<password>"));
        assertTrue(help.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitStopsInteractiveReading() throws IOException {
        UserManager users = users("users.txt");
        runner(users).run(new BufferedReader(new StringReader("EXIT\n" + CREATE_ANA + "\n")));
        assertNull(users.findByEmail("ana@correo.com"));
    }

    @Test
    void commandsAfterExitInFileAreNotExecuted() throws IOException {
        UserManager users = users("users.txt");
        runner(users).runFile(commandFile(CREATE_ANA, "EXIT", CREATE_PEDRO));
        assertNotNull(users.findByEmail("ana@correo.com"));
        assertNull(users.findByEmail("pedro@correo.com"));
    }
}
