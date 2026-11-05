package es.upm.amusementpark.integration;

import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.persistence.repository.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.storage.UserStorage;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.business.service.UserService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeliveryOneIntegrationTest {

    static final List<String> EXAMPLE = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT");

    @TempDir
    Path dir;

    static int runApp(String[] args, String stdin, ByteArrayOutputStream out, ByteArrayOutputStream err, Path users) {
        try {
            Method run = Class.forName("es.upm.amusementpark.presentation.App")
                    .getDeclaredMethod("run", String[].class, java.io.InputStream.class, PrintStream.class, PrintStream.class, Path.class);
            run.setAccessible(true);
            return (int) run.invoke(null, args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                    new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8), users.getParent());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void basicExampleInBothModes() throws IOException {
        Path a = Files.createDirectory(dir.resolve("a"));
        Path b = Files.createDirectory(dir.resolve("b"));
        Path input = dir.resolve("input.txt");
        Files.write(input, EXAMPLE, StandardCharsets.UTF_8);
        ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
        ByteArrayOutputStream interOut = new ByteArrayOutputStream();
        assertEquals(0, runApp(new String[] {input.toString()}, "", fileOut, new ByteArrayOutputStream(), a.resolve("users.txt")));
        assertEquals(0, runApp(new String[0], String.join("\n", EXAMPLE) + "\nUSER DELETE;ana.nueva@correo.com;Clave1!\n",
                interOut, new ByteArrayOutputStream(), b.resolve("users.txt")));
        String out = fileOut.toString(StandardCharsets.UTF_8);
        assertEquals(out, interOut.toString(StandardCharsets.UTF_8));
        List<String> lines = out.lines().toList();
        assertEquals(List.of("USER CREATED", "USER CREATED", "USER UPDATED", "USER UPDATED", "USER DELETED"), lines.subList(0, 5));
        assertTrue(out.contains("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>"));
        assertEquals("GOODBYE", lines.get(lines.size() - 1));
        List<String> expected = List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;<bcrypt>;699999999;1234567890123456");
        assertEquals(expected, masked(Files.readAllLines(a.resolve("users.txt"))));
        assertEquals(expected, masked(Files.readAllLines(b.resolve("users.txt"))));
    }

    @Test
    void failingSaveIsControlled() {
        UserStorage broken = new UserStorage() {
            @Override
            public List<User> load() {
                return List.of();
            }

            @Override
            public void save(List<User> users) {
                throw new es.upm.amusementpark.persistence.storage.PersistenceException("disk full", new IOException("disk full"));
            }
        };
        CommandProcessor p = ApplicationInitializer.initialize(Repositories.inMemory(new UserRepository(broken)));
        assertTrue(p.process(EXAMPLE.get(0)).startsWith("ERROR: data could not be saved"));
        assertTrue(p.process("HELP").contains("USER CREATE"));
    }

    @Test
    void malformedInputIsControlled() {
        CommandProcessor p = ApplicationInitializer.initialize(Repositories.inMemory(new UserRepository()));
        for (String line : List.of(";;;", "USER", "user create;a", "USER UPDATE;;;;", "EXIT;x")) {
            assertTrue(p.process(line).startsWith("ERROR"), line);
        }
    }

    @Test
    void unreadableUsersFileIsControlled() throws IOException {
        Path users = Files.createDirectory(dir.resolve("users.txt"));
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertEquals(1, runApp(new String[0], "", new ByteArrayOutputStream(), err, users));
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("ERROR"));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("users.txt"));
    }

    @Test
    void productionCodeRespectsRestrictions() throws IOException {
        List<String> forbidden = List.of("java.util.regex", ".matches(\"", "replaceAll(", "replaceFirst(",
                "split(\"\\\\", "javax.crypto", "MessageDigest", "Serializable", "ObjectOutputStream", "ObjectInputStream");
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path f : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String code = Files.readString(f);
                for (String token : forbidden) {
                    assertTrue(!code.contains(token), f + " uses " + token);
                }
            }
        }
        String pom = Files.readString(Path.of("pom.xml"));
        String deps = pom.substring(pom.indexOf("<dependencies>"), pom.indexOf("</dependencies>"));
        // every dependency is test-scoped except the mandated BCrypt library
        assertEquals(deps.split("<dependency>", -1).length - 2, deps.split("<scope>test</scope>", -1).length - 1);
        assertTrue(deps.contains("<artifactId>jbcrypt</artifactId>"));
    }

    /** Replaces the (salted BCrypt) password field of user file lines so they can be compared. */
    static java.util.List<String> masked(java.util.List<String> lines) {
        return lines.stream().map(l -> {
            String[] f = l.split(";", -1);
            if (f.length == 7) {
                f[4] = f[4].startsWith("$2a$") ? "<bcrypt>" : f[4];
            }
            return String.join(";", f);
        }).toList();
    }
}
