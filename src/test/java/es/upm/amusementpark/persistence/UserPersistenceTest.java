package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.App;
import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.User;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserPersistenceTest {

    private ParkController park;
    private ParkStorage storage;

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        storage = ParkStorage.inDirectory(dir);
        park.setRepository(storage);
    }

    @Test
    void usersRecoveredAfterRestart() throws IOException {
        park.registerUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.saveState();
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Clave1!"));
        park.reset();
        park.setRepository(storage);
        assertEquals(List.of(), park.loadState());
        User ana = park.findUser("ana@correo.com");
        assertEquals("Ana", ana.getFirstName());
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCard());
        assertTrue(park.verifyPassword("Clave1!", ana.getPassword()));
    }

    @Test
    void lineMappedToFieldsAndBadLinesWarned() throws IOException {
        String hash = park.protectPassword("Otra2#");
        Files.writeString(dir.resolve("users.txt"), "Pedro;Lopez Martin;87654321X;pedro@correo.com;" + hash
                + ";623456789;9876543210987654\nRoto;Solo;3\nOtro;X;87654321X;otro@correo.com;" + hash + ";1;2\n");
        park.reset();
        park.setRepository(storage);
        List<String> warnings = park.loadState();
        assertEquals(2, warnings.size(), warnings.toString());
        User pedro = park.findUser("pedro@correo.com");
        assertEquals("Pedro", pedro.getFirstName());
        assertEquals("Lopez Martin", pedro.getSurnames());
        assertEquals("87654321X", pedro.getDni());
        assertEquals("623456789", pedro.getPhone());
        assertEquals("9876543210987654", pedro.getCard());
        assertEquals(1, park.getUsers().size());
    }

    @Test
    void userCreatedInOneRunUsableInTheNext() {
        ByteArrayOutputStream first = new ByteArrayOutputStream();
        App.run(new String[0], new BufferedReader(new StringReader(
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nEXIT\n")),
                new PrintStream(first, true, StandardCharsets.UTF_8), dir);
        assertTrue(first.toString(StandardCharsets.UTF_8).contains("USER CREATED"));
        ByteArrayOutputStream second = new ByteArrayOutputStream();
        App.run(new String[0], new BufferedReader(new StringReader("TICKET CREATE;T001;ana@correo.com;"
                + java.time.LocalDate.now() + "\nEXIT\n")), new PrintStream(second, true, StandardCharsets.UTF_8), dir);
        assertTrue(second.toString(StandardCharsets.UTF_8).contains("TICKET CREATED. PRICE: 40.00"),
                second.toString(StandardCharsets.UTF_8));
        assertNotNull(park.findUser("ana@correo.com"));
    }
}
