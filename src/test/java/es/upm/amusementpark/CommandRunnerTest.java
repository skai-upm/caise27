package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.*;
import es.upm.amusementpark.business.exception.*;
import es.upm.amusementpark.business.model.*;
import es.upm.amusementpark.business.security.*;
import es.upm.amusementpark.persistence.file.*;
import es.upm.amusementpark.presentation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandRunnerTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private String runFile(String content) throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, content);
        return run(new String[] {input.toString()}, "");
    }

    private String run(String[] args, String stdin) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)), out, dir);
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private FileUserRepository storedUsers() {
        FileUserRepository repository = new FileUserRepository(dir.resolve("users.txt"));
        repository.load();
        return repository;
    }

    @Test
    void fileCommandsRunInOrderAndBlankLinesAreIgnored() throws IOException {
        String output = runFile(CREATE_ANA + "\n" + CREATE_PEDRO + "\n\n   \n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nEXIT\n");
        assertEquals(String.join(System.lineSeparator(), "USER CREATED", "USER CREATED", "USER UPDATED", "BYE", ""),
                output);
        assertEquals("699999999", storedUsers().findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void exitStopsLaterLines() throws IOException {
        runFile(CREATE_ANA + "\nEXIT\n" + CREATE_PEDRO + "\n");
        assertNotNull(storedUsers().findByEmail("ana@correo.com"));
        assertNull(storedUsers().findByEmail("pedro@correo.com"));
    }

    @Test
    void interactiveAndFileModesProduceTheSameOutput() throws IOException {
        String commands = CREATE_ANA + "\nUSER DELETE;ana@correo.com;Mala1!\nHELP\nEXIT\n";
        String fromFile = runFile(commands);
        Files.delete(dir.resolve("users.txt"));
        String fromStdin = run(new String[0], commands);
        assertEquals(fromFile, fromStdin);
    }

    @Test
    void interactiveModeEndsAtEndOfInput() {
        String output = run(new String[0], CREATE_ANA + "\n");
        assertEquals("USER CREATED" + System.lineSeparator(), output);
    }

    @Test
    void missingCommandFileIsReportedInAControlledWay() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int status = App.run(new String[] {dir.resolve("nope.txt").toString()}, new ByteArrayInputStream(new byte[0]),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), dir);
        assertEquals(1, status);
        assertTrue(bytes.toString(StandardCharsets.UTF_8).startsWith("ERROR: cannot read command file"));
    }

    @Test
    void directoryAsCommandFileIsReportedInAControlledWay() {
        String output = run(new String[] {dir.toString()}, "");
        assertTrue(output.startsWith("ERROR: cannot read command file"));
    }
}
