package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserUpdateIntegrationTest {

    private static String run(Path workDir, String stdin) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[0], new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void sourceExamplesPersist(@TempDir Path workDir) {
        String out = run(workDir, String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#",
                "USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666",
                "USER UPDATE;ana.nueva@correo.com;Nueva2#;DNI;87654321X",
                "USER UPDATE;ana.nueva@correo.com;Clave1!;PHONE;699999999",
                "EXIT"));
        assertEquals(4, out.split("USER UPDATED", -1).length - 1, out);
        assertEquals(2, out.split("ERROR", -1).length - 1, out);
        run(workDir, "EXIT\n");
        ParkController park = ParkController.getInstance();
        User ana = park.findUser("ana.nueva@correo.com").orElseThrow();
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("612345678", ana.getPhone());
        assertEquals("12345678Z", ana.getDni());
        park.authenticate("ana.nueva@correo.com", "Nueva2#");
        assertTrue(park.findUser("ana@correo.com").isEmpty());
    }
}
