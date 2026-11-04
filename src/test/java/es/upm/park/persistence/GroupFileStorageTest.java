package es.upm.park.persistence;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.User;
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

class GroupFileStorageTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoGroups() {
        assertTrue(new GroupFileStorage(dir.resolve("none.txt")).load(dni -> null).isEmpty());
    }

    @Test
    void loadResolvesKnownDnisAndSkipsUnknownOrMalformed() throws IOException {
        User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        Path file = dir.resolve("groups.txt");
        Files.write(file, List.of("G01;Familia Garcia;12345678Z;99999999R", "", "broken", "G02;Vacio"));
        List<Group> groups = new GroupFileStorage(file).load(Map.of("12345678Z", ana)::get);
        assertEquals(2, groups.size());
        assertEquals(1, groups.get(0).getMembers().size());
        assertSame(ana, groups.get(0).getMembers().get(0));
        assertTrue(groups.get(1).getMembers().isEmpty());
    }

    @Test
    void saveWritesIdNameAndDnis() throws IOException {
        Group g = new Group("G01", "Familia Garcia");
        g.add(new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456"));
        Path file = dir.resolve("groups.txt");
        new GroupFileStorage(file).save(List.of(g, new Group("G02", "Vacio")));
        assertEquals(List.of("G01;Familia Garcia;12345678Z", "G02;Vacio"), Files.readAllLines(file));
    }
}
