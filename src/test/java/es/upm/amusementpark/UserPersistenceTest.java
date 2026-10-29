package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.DataDirectories;
import es.upm.amusementpark.persistence.TextFileRepositories;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserPersistenceTest {

    private final ParkController park = ParkController.getInstance();

    @TempDir
    Path base;

    @BeforeEach
    void setUp() {
        park.reset();
    }

    private String app(String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(new String[0], base, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    private Path usersFile() {
        return DataDirectories.permanent(base).resolve("users.txt");
    }

    @Test
    void assignmentLineFormat() throws IOException {
        app("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nEXIT\n");
        List<String> lines = Files.readAllLines(usersFile(), StandardCharsets.UTF_8);
        assertEquals(1, lines.size());
        String[] f = lines.get(0).split(";", -1);
        assertEquals(7, f.length);
        assertEquals(List.of("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com"), List.of(f).subList(0, 4));
        assertTrue(f[4].startsWith("$2a$"));
        assertFalse(lines.get(0).contains("Clave1!"));
        assertEquals(List.of("612345678", "1234567890123456"), List.of(f).subList(5, 7));
    }

    @Test
    void usersReloadedAtStart() {
        app("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n"
                + "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n"
                + "EXIT\n");
        String out = app("SYSTEM DATE;2027-04-15\nTICKET CREATE;T001;pedro@correo.com;2027-04-15\n"
                + "USER CREATE;Ana;X;12345678Z;otra@correo.com;Clave1!;1;1234567890123456\nEXIT\n");
        assertTrue(out.contains("TICKET CREATED"));
        assertTrue(out.contains("a user with DNI 12345678Z already exists"));
        User pedro = park.findUser("pedro@correo.com");
        assertEquals("Pedro", pedro.getFirstName());
        assertEquals("Lopez Martin", pedro.getSurnames());
        assertEquals("9876543210987654", pedro.getCard());
        park.verifyPassword("pedro@correo.com", "Otra2#");
    }

    @Test
    void earlierEmailFirstLinesStillRead() throws IOException {
        Path dir = DataDirectories.permanent(base);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("users.txt"),
                "ana@correo.com;12345678Z;;Ana;Garcia Lopez;612345678;1234567890123456\n", StandardCharsets.UTF_8);
        park.configure(TextFileRepositories.create(dir));
        assertTrue(park.load().isEmpty());
        User ana = park.findUser("ana@correo.com");
        assertEquals("Ana", ana.getFirstName());
        assertEquals("12345678Z", ana.getDni());
    }
}
