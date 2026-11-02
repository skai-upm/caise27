package es.upm.etsisi.amusementpark.presentation;

import static es.upm.etsisi.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.etsisi.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
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

import es.upm.etsisi.amusementpark.App;
import es.upm.etsisi.amusementpark.persistence.FileRepositories;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.user.Passwords;
import es.upm.etsisi.amusementpark.business.user.UserManager;

class CommandRunnerTest {

    private static final String UPDATE_PHONE = "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999";

    @TempDir
    Path dir;

    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);

    /** Loads the park from its own data directory and returns its users. */
    private UserManager users(String name) throws IOException {
        Path data = Files.createDirectories(dir.resolve(name));
        ParkController.getInstance().configure(FileRepositories.in(data));
        return ParkController.getInstance().getUsers();
    }

    private CommandRunner runner(UserManager users) {
        return new CommandRunner(App.wire(ParkController.getInstance()), out);
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
        UserManager users = users("data");
        runner(users).run(new BufferedReader(new StringReader(CREATE_ANA + "\n" + UPDATE_PHONE + "\n")));
        assertEquals(List.of("USER CREATED", "USER UPDATED"), output().lines().toList());
        assertEquals("699999999", users.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void processesFileInOrderIgnoringBlankLines() throws IOException {
        UserManager users = users("data");
        assertTrue(runner(users).runFile(commandFile(CREATE_ANA, "", "   ", UPDATE_PHONE)));
        assertEquals(List.of("USER CREATED", "USER UPDATED"), output().lines().toList());
        assertEquals("699999999", users.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void interactiveAndFileModesAreEquivalent() throws IOException {
        String[] sequence = {CREATE_ANA, CREATE_PEDRO, "USER CREATE;bad", UPDATE_PHONE,
            "USER DELETE;pedro@correo.com;Otra2#", "HELP", "EXIT"};

        runner(users("interactive")).run(new BufferedReader(new StringReader(String.join("\n", sequence))));
        String interactiveOutput = output();
        bytes.reset();
        runner(users("file")).runFile(commandFile(sequence));

        assertEquals(interactiveOutput, output());
        List<String> interactive = Files.readAllLines(dir.resolve("interactive/users.txt"));
        List<String> file = Files.readAllLines(dir.resolve("file/users.txt"));
        // BCrypt salts differ between runs: compare every field but the hash, and verify both hashes
        assertEquals(withoutPassword(interactive), withoutPassword(file));
        for (String line : List.of(interactive.get(0), file.get(0))) {
            assertTrue(Passwords.matches("Clave1!", line.split(";")[4]));
        }
    }

    @Test
    void missingFileIsReportedInControlledWay() throws IOException {
        assertFalse(runner(users("data")).runFile(dir.resolve("does-not-exist.txt")));
        assertTrue(output().startsWith("ERROR: cannot read command file"));
    }

    @Test
    void directoryIsNotAReadableCommandFile() throws IOException {
        assertFalse(runner(users("data")).runFile(dir));
        assertTrue(output().startsWith("ERROR:"));
    }

    @Test
    void helpListsCommandSyntax() throws IOException {
        runner(users("data")).run(new BufferedReader(new StringReader("HELP\n")));
        String help = output();
        assertTrue(help.contains("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>"));
        assertTrue(help.contains("USER DELETE;<Email>;<Password>"));
        assertTrue(help.contains("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>"));
        for (String command : List.of("SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>",
                "GROUP CREATE;<Identifier>;<Name>", "GROUP ADD;<Identifier>;<Email>",
                "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
                "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attractionIdentifier>")) {
            assertTrue(help.contains(command), command);
        }
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitStopsInteractiveReading() throws IOException {
        UserManager users = users("data");
        runner(users).run(new BufferedReader(new StringReader("EXIT\n" + CREATE_ANA + "\n")));
        assertNull(users.findByEmail("ana@correo.com"));
    }

    @Test
    void commandsAfterExitInFileAreNotExecuted() throws IOException {
        UserManager users = users("data");
        runner(users).runFile(commandFile(CREATE_ANA, "EXIT", CREATE_PEDRO));
        assertNotNull(users.findByEmail("ana@correo.com"));
        assertNull(users.findByEmail("pedro@correo.com"));
    }

    private static List<String> withoutPassword(List<String> userLines) {
        return userLines.stream().map(l -> {
            String[] f = l.split(";", -1);
            f[4] = "*";
            return String.join(";", f);
        }).toList();
    }
}
