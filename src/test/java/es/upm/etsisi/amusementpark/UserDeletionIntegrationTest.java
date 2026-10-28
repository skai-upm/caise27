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

class UserDeletionIntegrationTest {

    private static String run(Path workDir, String stdin) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[0], new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void deletionIsPersisted(@TempDir Path workDir) {
        String out = run(workDir, String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER DELETE;pedro@correo.com;Otra2!",
                "USER DELETE;nadie@correo.com;Otra2#",
                "USER DELETE;pedro@correo.com;Otra2#",
                "EXIT"));
        assertEquals(2, out.split("ERROR", -1).length - 1, out);
        assertTrue(out.contains("USER DELETED"), out);
        run(workDir, "EXIT\n");
        ParkController park = ParkController.getInstance();
        assertTrue(park.findUser("pedro@correo.com").isEmpty());
        assertTrue(park.findUser("ana@correo.com").isPresent());
    }
}
