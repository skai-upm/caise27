package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandFileModeTest {

    private static final String NL = System.lineSeparator();
    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    private static final String UPDATE = "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999";

    private static String run(Path workDir, String stdin, String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void interactiveAndFileModesBehaveIdentically(@TempDir Path interactiveDir, @TempDir Path fileDir)
            throws Exception {
        String commands = String.join("\n", ANA, UPDATE, "HELP", "EXIT") + "\n";
        String interactive = run(interactiveDir, commands);
        assertEquals("699999999",
                ParkController.getInstance().findUser("ana@correo.com").orElseThrow().getPhone());
        Path file = fileDir.resolve("input.txt");
        Files.writeString(file, commands);
        String fromFile = run(fileDir, "", file.toString());
        assertEquals(interactive, fromFile);
        assertEquals("699999999",
                ParkController.getInstance().findUser("ana@correo.com").orElseThrow().getPhone());
        assertTrue(fromFile.startsWith("USER CREATED" + NL + "USER UPDATED" + NL + "Available commands:"), fromFile);
    }

    @Test
    void fileOrderBlankLinesAndExit(@TempDir Path workDir) throws Exception {
        Path file = workDir.resolve("input.txt");
        Files.writeString(file, String.join("\n", ANA, "", "   ", PEDRO, "", UPDATE, "EXIT",
                "USER DELETE;pedro@correo.com;Otra2#"));
        String out = run(workDir, "", file.toString());
        assertEquals("USER CREATED" + NL + "USER CREATED" + NL + "USER UPDATED" + NL + "Exiting application." + NL,
                out);
        assertTrue(ParkController.getInstance().findUser("pedro@correo.com").isPresent());
    }

    @Test
    void missingAndUnreadableFilesAreControlled(@TempDir Path workDir) throws Exception {
        Path missing = workDir.resolve("missing.txt");
        assertEquals("ERROR: Cannot read command file: " + missing + NL, run(workDir, "", missing.toString()));
        Path directory = Files.createDirectory(workDir.resolve("folder.txt"));
        assertEquals("ERROR: Cannot read command file: " + directory + NL, run(workDir, "", directory.toString()));
    }
}
