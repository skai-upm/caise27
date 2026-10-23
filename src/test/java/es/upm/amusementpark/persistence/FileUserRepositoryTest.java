package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.User;
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

class FileUserRepositoryTest {

    static final String ANA_HASH = "$2a$04$ZfC3Vb2iU1N6c4k3bS0e7eQ0gTqQkzXQe4v2dYb0xJ2m0q1QpWb6S";
    static final String PEDRO_HASH = "$2a$04$hKf8VdQ0x7Jt0a1b2c3d4eFGHijklmnopqrstuvwxyzABCDEFGHIJ";
    private static final String ANA_LINE =
            "Ana;Garcia Lopez;12345678Z;ana@correo.com;" + ANA_HASH + ";612345678;1234567890123456";
    private static final String PEDRO_LINE =
            "Pedro;Lopez Martin;87654321X;pedro@correo.com;" + PEDRO_HASH + ";623456789;9876543210987654";

    @TempDir
    Path dir;

    private Path file;
    private ByteArrayOutputStream warnings;
    private FileUserRepository repository;

    @BeforeEach
    void setUp() {
        file = dir.resolve(FileUserRepository.DEFAULT_FILE_NAME);
        warnings = new ByteArrayOutputStream();
        repository = new FileUserRepository(file, new PrintStream(warnings, true, StandardCharsets.UTF_8));
    }

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", ANA_HASH, "612345678",
                "1234567890123456");
    }

    private User pedro() {
        return new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", PEDRO_HASH, "623456789",
                "9876543210987654");
    }

    @Test
    void missingFileMeansNoUsers() throws IOException {
        assertTrue(repository.loadAll().isEmpty());
    }

    @Test
    void writesOneLinePerUserInSpecifiedFormat() throws IOException {
        repository.saveAll(List.of(ana(), pedro()));
        assertEquals(List.of(ANA_LINE, PEDRO_LINE), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void roundTripsUsers() throws IOException {
        repository.saveAll(List.of(ana(), pedro()));
        List<User> loaded = new FileUserRepository(file).loadAll();
        assertEquals(2, loaded.size());
        assertEquals(ANA_LINE, FileUserRepository.format(loaded.get(0)));
        assertEquals(PEDRO_LINE, FileUserRepository.format(loaded.get(1)));
    }

    @Test
    void overwritesPreviousContent() throws IOException {
        repository.saveAll(List.of(ana(), pedro()));
        repository.saveAll(List.of(pedro()));
        assertEquals(List.of(PEDRO_LINE), Files.readAllLines(file, StandardCharsets.UTF_8));
        try (var stream = Files.list(dir)) {
            assertEquals(1, stream.count(), "temporary file must not be left behind");
        }
    }

    @Test
    void skipsPlainTextPasswordsFromEarlierVersions() throws IOException {
        Files.write(file, List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                PEDRO_LINE), StandardCharsets.UTF_8);
        List<User> loaded = repository.loadAll();
        assertEquals(1, loaded.size());
        assertEquals("pedro@correo.com", loaded.get(0).getEmail());
        assertTrue(warnings.toString(StandardCharsets.UTF_8).contains("line 1"));
    }

    @Test
    void skipsMalformedInvalidAndDuplicateLinesWithWarning() throws IOException {
        Files.write(file, List.of(
                ANA_LINE,
                "",
                "Broken;line;with;too;few",
                "Bad;Dni;12345678A;bad@correo.com;" + ANA_HASH + ";6;1234567890123456",
                "Dup;Email;87654321X;ana@correo.com;" + ANA_HASH + ";6;1234567890123456",
                PEDRO_LINE), StandardCharsets.UTF_8);
        List<User> loaded = repository.loadAll();
        assertEquals(2, loaded.size());
        assertEquals("ana@correo.com", loaded.get(0).getEmail());
        assertEquals("pedro@correo.com", loaded.get(1).getEmail());
        String text = warnings.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("line 3"));
        assertTrue(text.contains("line 4"));
        assertTrue(text.contains("line 5"));
        assertFalse(text.contains("line 2"));
    }
}
