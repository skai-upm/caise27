package es.upm.etsisi.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserFileFormatTest {

    @TempDir
    Path directory;

    @Test
    void assignmentLineFormatWithProtectedPassword() throws Exception {
        TextUserRepository repository = new TextUserRepository(directory.resolve("users.txt"));
        String hash = new BCryptPasswordHasher().hash("Clave1!");
        repository.save(List.of(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", hash, "612345678",
                "1234567890123456")));
        String line = Files.readAllLines(directory.resolve("users.txt")).get(0);
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com;" + hash + ";612345678;1234567890123456", line);
        assertFalse(line.contains("Clave1!"));
        User loaded = repository.load().get(0);
        assertEquals("Garcia Lopez", loaded.getSurnames());
        assertEquals(hash, loaded.getPasswordHash());
        assertEquals("1234567890123456", loaded.getCard());
    }

    @Test
    void earlierFormatsStillLoad() throws Exception {
        Files.writeString(directory.resolve("users.txt"), String.join("\n",
                "12345678Z;ana@correo.com",
                "87654321X;pedro@correo.com;$2a$10$abc",
                "11111111H;luis@correo.com;;Luis;Perez Gil;611111111;4111111111111111",
                "not a user line"));
        TextUserRepository repository = new TextUserRepository(directory.resolve("users.txt"));
        List<User> users = repository.load();
        assertEquals(3, users.size());
        assertNull(users.get(0).getPasswordHash());
        assertEquals("$2a$10$abc", users.get(1).getPasswordHash());
        assertEquals("Luis", users.get(2).getFirstName());
        assertEquals("11111111H", users.get(2).getDni());
        assertEquals(1, repository.getProblems().size());
    }

    @Test
    void userAndMainClassesDoNotHandleFiles() throws Exception {
        for (String source : List.of("business/model/User.java", "App.java")) {
            String code = Files.readString(Path.of("src/main/java/es/upm/etsisi/amusementpark/" + source));
            assertFalse(code.contains("users.txt"), source);
            assertTrue(!source.startsWith("business") || !code.contains("java.nio"), source);
        }
    }
}
