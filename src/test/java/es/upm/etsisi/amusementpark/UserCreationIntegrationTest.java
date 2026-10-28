package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.ParkController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserCreationIntegrationTest {

    private static String run(Path workDir, String stdin) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[0], new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static String readFile(Path file) {
        try {
            return java.nio.file.Files.readString(file);
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void createUsersFromCommandsAndRestart(@TempDir Path workDir) {
        String out = run(workDir, String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER CREATE;Ana;Garcia;12345678Z;otra@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Ana;Garcia Lopez",
                "USER CREATE;Luis;Perez;11111111H;luis@correo.es;Clave1!;611111111;1234567890123456",
                "HELP",
                "EXIT"));
        assertEquals(2, out.split("USER CREATED", -1).length - 1, out);
        assertEquals(3, out.split("ERROR", -1).length - 1, out);
        assertTrue(out.contains("Available commands:"), out);
        String file = readFile(workDir.resolve("data").resolve("users.txt"));
        assertTrue(file.startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$"), file);
        assertTrue(file.contains(";612345678;1234567890123456"), file);
        run(workDir, "EXIT\n");
        ParkController park = ParkController.getInstance();
        assertEquals(2, park.getUsers().size());
        assertEquals("Lopez Martin", park.findUser("pedro@correo.com").orElseThrow().getSurnames());
        park.authenticate("pedro@correo.com", "Otra2#");
    }
}
