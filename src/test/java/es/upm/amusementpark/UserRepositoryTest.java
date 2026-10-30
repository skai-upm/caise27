package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.*;
import es.upm.amusementpark.business.exception.*;
import es.upm.amusementpark.business.model.*;
import es.upm.amusementpark.business.security.*;
import es.upm.amusementpark.persistence.file.*;
import es.upm.amusementpark.presentation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoUsers() {
        FileUserRepository repository = new FileUserRepository(dir.resolve("users.txt"));
        repository.load();
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void writesOneLinePerUserInSourceFormat() throws IOException {
        Path file = dir.resolve("users.txt");
        FileUserRepository repository = new FileUserRepository(file);
        repository.add(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456"));
        repository.add(new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654"));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    void roundTripsUsersBetweenInstances() {
        Path file = dir.resolve("users.txt");
        FileUserRepository first = new FileUserRepository(file);
        first.add(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456"));

        FileUserRepository second = new FileUserRepository(file);
        second.load();
        User ana = second.findByDni("12345678Z");
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals(ana, second.findByEmail("ana@correo.com"));
    }

    @Test
    void removalIsPersisted() {
        Path file = dir.resolve("users.txt");
        FileUserRepository repository = new FileUserRepository(file);
        User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        repository.add(ana);
        repository.remove(ana);

        FileUserRepository reloaded = new FileUserRepository(file);
        reloaded.load();
        assertNull(reloaded.findByEmail("ana@correo.com"));
    }

    @Test
    void skipsMalformedLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.writeString(file, "broken;line\n\nAna;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;"
                + "1234567890123456\n");
        FileUserRepository repository = new FileUserRepository(file);
        repository.load();
        assertEquals(1, repository.findAll().size());
    }
}
