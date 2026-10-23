package es.upm.amusementpark.presentation;

import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.persistence.FileUserRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Commands run against the real file repository, checking that every change
 * is reflected in the user file and survives a restart.
 */
class UserFileCommandsTest {

    @TempDir
    Path dir;

    private Path file;

    @BeforeEach
    void setUp() {
        file = dir.resolve(FileUserRepository.DEFAULT_FILE_NAME);
    }

    private CommandProcessor start() {
        return new CommandProcessor(TestParks.open(dir));
    }

    private List<String> fileLines() throws IOException {
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    /** File lines with the password hash replaced by {@code <hash>}. */
    private List<String> maskedLines() throws IOException {
        return fileLines().stream().map(UserFileCommandsTest::mask).toList();
    }

    static String mask(String line) {
        String[] fields = line.split(";", -1);
        fields[4] = "<hash>";
        return String.join(";", fields);
    }

    private String storedHash(int line) throws IOException {
        return fileLines().get(line).split(";", -1)[4];
    }

    @Test
    void createdUsersSurviveRestart() throws IOException {
        CommandProcessor first = start();
        assertEquals("USER CREATED", first.process(CREATE_ANA).output());
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;<hash>;612345678;1234567890123456"),
                maskedLines());
        assertTrue(TestParks.HASHER.matches("Clave1!", storedHash(0)));
        assertFalse(fileLines().get(0).contains("Clave1!"));

        CommandProcessor second = start();
        assertTrue(second.process(CREATE_ANA).output().startsWith("ERROR"));
        assertEquals("USER DELETED", second.process("USER DELETE;ana@correo.com;Clave1!").output());
    }

    @Test
    void deletionRemovesLineFromFile() throws IOException {
        CommandProcessor processor = start();
        processor.process(CREATE_ANA);
        processor.process(CREATE_PEDRO);
        assertEquals("USER DELETED", processor.process("USER DELETE;pedro@correo.com;Otra2#").output());
        assertEquals(1, fileLines().size());
        assertTrue(fileLines().get(0).startsWith("Ana;"));

        assertTrue(start().process("USER DELETE;pedro@correo.com;Otra2#").output().startsWith("ERROR"));
    }

    @Test
    void wrongPasswordKeepsUserInFile() throws IOException {
        CommandProcessor processor = start();
        processor.process(CREATE_PEDRO);
        String output = processor.process("USER DELETE;pedro@correo.com;Wrong1!").output();
        assertTrue(output.startsWith("ERROR"));
        assertEquals(1, fileLines().size());
    }

    @Test
    void updatesArePersistedAndReloaded() throws IOException {
        CommandProcessor processor = start();
        processor.process(CREATE_ANA);
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999").output());
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").output());
        assertEquals("USER UPDATED",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").output());
        assertEquals(List.of("Ana Maria;Garcia Lopez;12345678Z;ana.nueva@correo.com;<hash>;699999999;"
                + "1234567890123456"), maskedLines());

        CommandProcessor restarted = start();
        assertTrue(restarted.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;1").output().startsWith("ERROR"));
        assertEquals("USER UPDATED",
                restarted.process("USER UPDATE;ana.nueva@correo.com;Clave1!;password;Nueva2#").output());
        assertEquals("USER UPDATED",
                restarted.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666").output());
        assertTrue(maskedLines().get(0).endsWith(";<hash>;699999999;9999888877776666"));
        assertTrue(TestParks.HASHER.matches("Nueva2#", storedHash(0)));
        assertFalse(TestParks.HASHER.matches("Clave1!", storedHash(0)));
    }

    @Test
    void updateRejectsDniAndUnknownFields() throws IOException {
        CommandProcessor processor = start();
        processor.process(CREATE_ANA);
        String output = processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").output();
        assertTrue(output.startsWith("ERROR"));
        assertTrue(output.contains("NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30").output().startsWith("ERROR"));
        assertTrue(fileLines().get(0).contains(";12345678Z;"));
    }

    @Test
    void updateReportsInvalidValueAndDuplicateEmail() throws IOException {
        CommandProcessor processor = start();
        processor.process(CREATE_ANA);
        processor.process(CREATE_PEDRO);
        List<String> before = fileLines();
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;CARD;1234").output().startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com").output()
                .startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Wrong1!;PHONE;699999999").output()
                .startsWith("ERROR"));
        assertEquals(before, fileLines());
    }

    @Test
    void updateWithWrongArgumentCountShowsSyntax() throws IOException {
        String output = start().process("USER UPDATE;ana@correo.com;Clave1!;PHONE").output();
        assertTrue(output.startsWith("ERROR"));
        assertTrue(output.contains(CommandProcessor.USER_UPDATE_SYNTAX));
    }

    @Test
    void deleteWithWrongArgumentCountShowsSyntax() throws IOException {
        String output = start().process("USER DELETE;ana@correo.com").output();
        assertTrue(output.startsWith("ERROR"));
        assertTrue(output.contains(CommandProcessor.USER_DELETE_SYNTAX));
    }
}
