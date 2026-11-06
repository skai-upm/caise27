package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.ParkController;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandFileInputTest {

    private static final String FLOW = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER DELETE;ana@correo.com;Clave1!",
            "HELP",
            "EXIT",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");

    @TempDir
    Path dir;

    private static String run(Path base, String stdin, String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new BufferedReader(new StringReader(stdin)), new PrintStream(buffer, true, StandardCharsets.UTF_8),
                base);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fileAndStdinBehaveIdentically() throws IOException {
        Path file = dir.resolve("input.txt");
        Files.writeString(file, FLOW);
        String fromStdin = run(dir.resolve("a"), FLOW);
        String fromFile = run(dir.resolve("b"), "", file.toString());
        assertEquals(fromStdin, fromFile);
        assertTrue(fromFile.contains("USER CREATED"));
        assertTrue(fromFile.contains("USER UPDATED"));
        assertTrue(fromFile.contains("USER DELETED"));
        assertEquals(1, fromFile.split("USER CREATED", -1).length - 1);
    }

    @Test
    void fileOrderPreserved() throws IOException {
        Path file = dir.resolve("order.txt");
        Files.writeString(file, "GROUP CREATE;G01;Uno\nGROUP DELETE;G01\nGROUP CREATE;G01;Dos\n");
        String out = run(dir.resolve("c"), "", file.toString());
        int created = out.indexOf("GROUP CREATED");
        int deleted = out.indexOf("GROUP DELETED");
        assertTrue(created >= 0 && deleted > created && out.lastIndexOf("GROUP CREATED") > deleted, out);
        assertNotNull(ParkController.getInstance().findGroup("G01"));
        assertEquals("Dos", ParkController.getInstance().findGroup("G01").getName());
    }

    @Test
    void unreadableFilesHandled() throws IOException {
        String missing = dir.resolve("missing.txt").toString();
        assertEquals("ERROR: Cannot read command file: " + missing + System.lineSeparator(),
                run(dir.resolve("d"), "", missing));
        Path directory = Files.createDirectories(dir.resolve("folder"));
        assertTrue(run(dir.resolve("e"), "", directory.toString())
                .startsWith("ERROR: Cannot read command file: " + directory));
    }
}
