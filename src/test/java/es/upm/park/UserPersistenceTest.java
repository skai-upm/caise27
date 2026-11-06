package es.upm.park;

import es.upm.park.business.controller.ParkControllerTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserPersistenceTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path base;

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
    }

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private String run(String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(args, base, new BufferedReader(new StringReader(stdin)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(new ByteArrayOutputStream()));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void usersSurviveRestartWithoutPlainPasswords() throws IOException {
        assertEquals(2, run(ANA + "\n" + PEDRO + "\nEXIT\n").split("USER CREATED", -1).length - 1);
        String file = Files.readString(base.resolve("data").resolve("users.txt"));
        assertTrue(file.startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$"), file);
        assertFalse(file.contains("Clave1!") || file.contains("Otra2#"));

        ParkControllerTestSupport.reset();
        String today = java.time.LocalDate.now().toString();
        String out = run(ANA + "\nTICKET CREATE;T001;pedro@correo.com;" + today + "\nEXIT\n");
        assertTrue(out.contains("ERROR: A user with email ana@correo.com already exists"), out);
        assertTrue(out.contains("TICKET CREATED"), out);
    }

    @Test
    void flyRunIsolatesUsers() throws IOException {
        run(ANA + "\nEXIT\n");
        String before = Files.readString(base.resolve("data").resolve("users.txt"));
        ParkControllerTestSupport.reset();
        String today = java.time.LocalDate.now().toString();
        String out = run(PEDRO + "\nTICKET CREATE;T001;ana@correo.com;" + today + "\nEXIT\n", "--fly");
        assertTrue(out.contains("USER CREATED"), out);
        assertTrue(out.contains("ERROR: No registered user with email ana@correo.com"), out);
        assertEquals(before, Files.readString(base.resolve("data").resolve("users.txt")));
    }
}
