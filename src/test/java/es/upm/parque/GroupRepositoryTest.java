package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupRepositoryTest {

    @TempDir
    Path dir;

    private final User ana = UserManagerTest.ana();
    private final User pedro = UserManagerTest.pedro();
    private final Map<String, User> users = Map.of(ana.getDni(), ana, pedro.getDni(), pedro);

    @Test
    void missingFileMeansNoGroups() throws IOException {
        assertTrue(new GroupRepository(dir.resolve("groups.txt")).load(users::get).isEmpty());
    }

    @Test
    void savesAndLoadsGroupsWithMemberReferences() throws IOException {
        Path file = dir.resolve("data").resolve("groups.txt");
        GroupRepository repository = new GroupRepository(file);
        Group familia = new Group("G01", "Familia Garcia");
        familia.addMember(ana);
        familia.addMember(pedro);
        repository.save(List.of(familia, new Group("G02", "Vacio")));
        assertEquals(List.of("G01;Familia Garcia;12345678Z,87654321X", "G02;Vacio;"), Files.readAllLines(file));
        List<Group> loaded = repository.load(users::get);
        assertEquals(2, loaded.size());
        assertEquals("Familia Garcia", loaded.get(0).getName());
        assertSame(ana, loaded.get(0).getMembers().get(0));
        assertSame(pedro, loaded.get(0).getMembers().get(1));
        assertTrue(loaded.get(1).getMembers().isEmpty());
    }

    @Test
    void ignoresUnknownDnisAndMalformedLines() throws IOException {
        Path file = dir.resolve("groups.txt");
        Files.write(file, List.of("roto", ";sin id;", "G01;Familia;12345678Z,00000000T"));
        List<Group> loaded = new GroupRepository(file).load(users::get);
        assertEquals(1, loaded.size());
        assertEquals(List.of(ana), loaded.get(0).getMembers());
    }
}
