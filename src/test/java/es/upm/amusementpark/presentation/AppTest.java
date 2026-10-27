package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.service.ParkController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    @TempDir
    Path tempDir;

    private String runApp(String[] args, String stdin) {
        return runApp(args, stdin, tempDir.resolve("data"));
    }

    private static String runApp(String[] args, String stdin, Path dataDir) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), dataDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private Path writeFile(String content) throws IOException {
        Path file = tempDir.resolve("input.txt");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void stdinAndFileProduceSameOutput() throws IOException {
        String commands = "HELP\nEXIT\n";
        String fromStdin = runApp(new String[0], commands);
        String fromFile = runApp(new String[] {writeFile(commands).toString()}, "");
        assertTrue(fromStdin.contains("USER DELETE;<Email>;<Password>"));
        assertEquals(fromStdin, fromFile);
    }

    @Test
    void fileLinesAfterExitAreNotProcessed() throws IOException {
        String out = runApp(new String[] {writeFile("EXIT\nHELP\n").toString()}, "");
        assertFalse(out.contains("USER DELETE"));
    }

    @Test
    void stdinLinesAfterExitAreNotProcessed() {
        String out = runApp(new String[0], "EXIT\nHELP\n");
        assertFalse(out.contains("USER DELETE"));
    }

    @Test
    void missingFileIsReportedInControlledManner() {
        String out = runApp(new String[] {tempDir.resolve("missing.txt").toString()}, "HELP\n");
        assertTrue(out.startsWith("ERROR: command file not found"));
        assertFalse(out.contains("Available commands"));
    }

    @Test
    void unreadableFileIsReportedInControlledManner() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("folder.txt"));
        String out = runApp(new String[] {directory.toString()}, "");
        assertTrue(out.startsWith("ERROR: command file cannot be read"));
    }

    @Test
    void usersPersistBetweenRuns() throws IOException {
        String create = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n";
        assertTrue(runApp(new String[0], create + "EXIT\n").contains("USER CREATED"));
        String[] stored = Files.readString(tempDir.resolve("data/users.txt"), StandardCharsets.UTF_8).trim().split(";");
        assertEquals(List.of("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "612345678", "1234567890123456"),
                List.of(stored[0], stored[1], stored[2], stored[3], stored[5], stored[6]));
        assertTrue(BCrypt.checkpw("Clave1!", stored[4]));
        assertFalse(Files.readString(tempDir.resolve("data/users.txt")).contains("Clave1!"));
        String second = runApp(new String[0], create + "EXIT\n");
        assertFalse(second.contains("USER CREATED"));
        assertTrue(second.contains("ERROR"));
    }

    @Test
    void deletedUserIsRemovedFromPersistedData() throws IOException {
        String create = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n";
        runApp(new String[0], create + "EXIT\n");
        assertTrue(runApp(new String[0], "USER DELETE;ana@correo.com;Clave1!\nEXIT\n").contains("USER DELETED"));
        assertTrue(Files.readString(tempDir.resolve("data/users.txt"), StandardCharsets.UTF_8).isBlank());
        assertTrue(runApp(new String[0], create + "EXIT\n").contains("USER CREATED"));
    }

    @Test
    void updatePersistsBetweenRuns() throws IOException {
        String create = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n";
        runApp(new String[0], create + "EXIT\n");
        assertTrue(runApp(new String[0], "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nEXIT\n")
                .contains("USER UPDATED"));
        assertTrue(Files.readString(tempDir.resolve("data/users.txt"), StandardCharsets.UTF_8)
                .contains(";699999999;"));
    }

    private static final String FILE_EXAMPLE = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "   ",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "EXIT",
            "");

    @Test
    void fileCommandsRunInOrderIgnoringBlankLines() throws IOException {
        String out = runApp(new String[] {writeFile(FILE_EXAMPLE).toString()}, "");
        String[] lines = out.split(System.lineSeparator());
        assertEquals("USER CREATED", lines[0]);
        assertEquals("USER CREATED", lines[1]);
        assertEquals("USER UPDATED", lines[2]);
        assertEquals("Exiting application.", lines[3]);
        assertEquals(4, lines.length);
    }

    @Test
    void fileAndInteractiveModesProduceSameOutput() throws IOException {
        String fromFile = runApp(new String[] {writeFile(FILE_EXAMPLE).toString()}, "", tempDir.resolve("a"));
        String fromStdin = runApp(new String[0], FILE_EXAMPLE, tempDir.resolve("b"));
        assertEquals(fromStdin, fromFile);
        assertEquals(withoutPasswords(tempDir.resolve("a/users.txt")), withoutPasswords(tempDir.resolve("b/users.txt")));
    }

    @Test
    void priceAndDateAreResetForEachExecution() {
        runApp(new String[0], "SYSTEM PRICE;45.50\nSYSTEM DATE;2027-04-15\n");
        assertEquals(new BigDecimal("45.50"), ParkController.getInstance().getCurrentPrice());
        runApp(new String[0], "EXIT\n");
        assertEquals(new BigDecimal("40.00"), ParkController.getInstance().getCurrentPrice());
        assertEquals(LocalDate.now(), ParkController.getInstance().getCurrentDate());
    }

    /** The users file with the password field removed (BCrypt values use random salts). */
    static List<String> withoutPasswords(Path usersFile) throws IOException {
        return Files.readAllLines(usersFile, StandardCharsets.UTF_8).stream()
                .map(line -> {
                    String[] f = line.split(";", -1);
                    f[4] = "";
                    return String.join(";", f);
                })
                .toList();
    }
}
