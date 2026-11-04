package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupFileRepositoryTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "2", "9876543210987654");
    private final Map<String, User> byDni = Map.of(ana.getDni(), ana, pedro.getDni(), pedro);

    @Test
    void missingFileMeansNoGroups() throws Exception {
        assertTrue(new GroupFileRepository(dir.resolve("groups.txt")).load(byDni::get).isEmpty());
    }

    @Test
    void savesExactLineAndRestoresSharedUsers() throws Exception {
        Path file = dir.resolve("groups.txt");
        Group g1 = new Group("G01", "Familia Garcia");
        g1.addMember(ana);
        g1.addMember(pedro);
        Group g2 = new Group("G02", "Amigos");
        g2.addMember(ana);
        Group empty = new Group("G03", "Vacio");
        new GroupFileRepository(file).save(List.of(g1, g2, empty));

        assertEquals(List.of("G01;Familia Garcia;12345678Z;87654321X", "G02;Amigos;12345678Z", "G03;Vacio"),
                Files.readAllLines(file, StandardCharsets.UTF_8));

        List<Group> loaded = new GroupFileRepository(file).load(byDni::get);
        assertEquals(3, loaded.size());
        assertSame(ana, loaded.get(0).getMembers().get(0));
        assertSame(ana, loaded.get(1).getMembers().get(0));
        assertTrue(loaded.get(2).getMembers().isEmpty());
    }

    @Test
    void ignoresUnknownMembersAndMalformedLines() throws Exception {
        Path file = dir.resolve("groups.txt");
        Files.write(file, List.of("G01;Familia;12345678Z;00000000T", "broken", ""), StandardCharsets.UTF_8);
        List<Group> loaded = new GroupFileRepository(file).load(byDni::get);
        assertEquals(1, loaded.size());
        assertEquals(List.of(ana), loaded.get(0).getMembers());
    }
}
