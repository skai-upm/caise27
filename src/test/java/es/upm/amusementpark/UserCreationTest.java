package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.presentation.CommandProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserCreationTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n";

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    @TempDir
    Path base;

    @BeforeEach
    void setUp() {
        park.reset();
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void sourceExampleCreatesUser() throws IOException {
        assertTrue(run(ANA).contains("USER CREATED"));
        User ana = park.findUser("ana@correo.com");
        assertEquals("Ana", ana.getFirstName());
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCard());
        assertFalse(ana.getPasswordHash().contains("Clave1!"));
        park.verifyPassword("ana@correo.com", "Clave1!");
    }

    @Test
    void invalidDataRejected() throws IOException {
        String out = run("USER CREATE;Ana;G;12345678A;ana@correo.com;Clave1!;6;1234567890123456\n"
                + "USER CREATE;Ana;G;12345678Z;ana@correo.com;clave1!;6;1234567890123456\n"
                + "USER CREATE;Ana;G;12345678Z;ana@correo.com;Clave1!;6;1234 5678 9012 3456\n"
                + "USER CREATE;Ana;G;12345678Z;ana@correo.es;Clave1!;6;1234567890123456\n"
                + "USER CREATE;Ana;Garcia Lopez;12345678Z\n"
                + "HELP\n");
        assertEquals(5, out.split("Error:", -1).length - 1);
        assertTrue(out.contains("invalid user data: DNI"));
        assertTrue(out.contains("Available commands:"));
        assertTrue(park.getUsers().isEmpty());
    }

    @Test
    void duplicateDniOrEmailRejected() throws IOException {
        String out = run(ANA
                + "USER CREATE;Otra;X;12345678Z;otra@correo.com;Clave1!;6;1234567890123456\n"
                + "USER CREATE;Pedro;Lopez Martin;87654321X;ana@correo.com;Otra2#;623456789;9876543210987654\n");
        assertTrue(out.contains("a user with DNI 12345678Z already exists"));
        assertTrue(out.contains("a user with email ana@correo.com already exists"));
        assertEquals(1, park.getUsers().size());
        assertNull(park.findUser("otra@correo.com"));
    }

    @Test
    void createdUserCanBuyTickets() throws IOException {
        String out = run(ANA + "SYSTEM DATE;2027-04-15\nTICKET CREATE;T001;ana@correo.com;2027-04-15\n");
        assertTrue(out.contains("TICKET CREATED. PRICE: 40.00 EUR"));
    }

    private String app(String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(args, base, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void persistedInNormalModeNotWithFly() throws IOException {
        app(ANA + "EXIT\n", App.FLY);
        try (Stream<Path> left = Files.list(base.resolve(App.TEMP_DIRECTORY))) {
            assertFalse(left.findAny().isPresent());
        }
        app("EXIT\n");
        assertNull(park.findUser("ana@correo.com"));
        app(ANA + "EXIT\n");
        app("EXIT\n");
        assertNotNull(park.findUser("ana@correo.com"));
        park.verifyPassword("ana@correo.com", "Clave1!");
    }
}
