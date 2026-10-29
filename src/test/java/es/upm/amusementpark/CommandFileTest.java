package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandFileTest {

    private static final String SEQUENCE =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n"
            + "\n"
            + "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n"
            + "   \n"
            + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\n"
            + "EXIT\n";

    private final ParkController park = ParkController.getInstance();

    @TempDir
    Path tmp;

    private String run(Path base, String stdin, String... args) throws IOException {
        Files.createDirectories(base);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(args, base, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fileAndInteractiveInputBehaveTheSame() throws IOException {
        Path file = tmp.resolve("input.txt");
        Files.writeString(file, SEQUENCE, StandardCharsets.UTF_8);
        String fromFile = run(tmp.resolve("file-run"), "", file.toString());
        assertEquals("699999999", park.findUser("ana@correo.com").getPhone());
        assertEquals("Pedro", park.findUser("pedro@correo.com").getFirstName());

        String fromStdin = run(tmp.resolve("stdin-run"), SEQUENCE);
        assertEquals(fromFile, fromStdin);
        assertEquals("USER CREATED\nUSER CREATED\nUSER UPDATED\nExiting application.\n",
                fromFile.replace("\r\n", "\n"));
    }

    @Test
    void unreadableFileIsControlled() throws IOException {
        Path directory = Files.createDirectory(tmp.resolve("not-a-file"));
        String out = run(tmp.resolve("base"), "", directory.toString());
        assertTrue(out.contains("Error: could not read command file: " + directory));
        assertFalse(out.contains("Exception"));
    }

    @Test
    void interactiveModeProcessesEveryCommandUntilEndOfInput() throws IOException {
        String out = run(tmp.resolve("base"), "SYSTEM PRICE;45\nHELP\nSYSTEM DATE;2027-04-15\n");
        assertTrue(out.contains("SYSTEM PRICE UPDATED"));
        assertTrue(out.contains("Available commands:"));
        assertTrue(out.contains("SYSTEM DATE UPDATED: 2027-04-15"));
        assertFalse(out.contains("Exiting application."));
    }
}
