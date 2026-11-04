package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupFileStoreTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
            "Clave1!", "612345678", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com",
            "Otra2#", "623456789", "9876543210987654");
    private final Map<String, User> byDni = Map.of(ana.getDni(), ana, pedro.getDni(), pedro);

    private Optional<User> lookup(String dni) {
        return Optional.ofNullable(byDni.get(dni));
    }

    @Test
    void writesIdNameAndMemberDnis() throws IOException {
        Path file = dir.resolve("groups.txt");
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        g.addMember(pedro);
        new GroupFileStore(file).save(List.of(g, new Group("G02", "Amigos")));
        assertEquals(List.of("G01;Familia Garcia;12345678Z;87654321X", "G02;Amigos"), Files.readAllLines(file));
    }

    @Test
    void loadResolvesTheSameUserObjects() throws IOException {
        Path file = dir.resolve("groups.txt");
        Files.write(file, List.of("G01;Familia Garcia;12345678Z;87654321X", "G02;Amigos;12345678Z"));
        List<Group> groups = new GroupFileStore(file).load(this::lookup);
        assertEquals(2, groups.size());
        assertSame(ana, groups.get(0).getMembers().get(0));
        assertSame(pedro, groups.get(0).getMembers().get(1));
        assertSame(ana, groups.get(1).getMembers().get(0));
    }

    @Test
    void missingFileMeansNoGroups() throws IOException {
        assertTrue(new GroupFileStore(dir.resolve("groups.txt")).load(this::lookup).isEmpty());
    }

    @Test
    void skipsMalformedLinesAndUnknownMembers() throws IOException {
        Path file = dir.resolve("groups.txt");
        Files.write(file, List.of("G01", ";Sin id", "", "G02;Amigos;99999999R;87654321X"));
        List<Group> groups = new GroupFileStore(file).load(this::lookup);
        assertEquals(1, groups.size());
        assertEquals(List.of(pedro), groups.get(0).getMembers());
    }
}
