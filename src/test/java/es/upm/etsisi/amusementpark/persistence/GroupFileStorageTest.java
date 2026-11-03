package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupFileStorageTest {

    @TempDir
    Path dir;

    final User ana = TestData.user("12345678Z", "ana@correo.com");
    final User pedro = TestData.user("87654321X", "pedro@correo.com");

    @Test
    void missingFileMeansNoGroups() {
        assertTrue(new GroupFileStorage(dir.resolve("none.txt")).load(List.of(ana)).isEmpty());
    }

    @Test
    void savesIdNameAndMemberDnis() throws IOException {
        Path file = dir.resolve("groups.txt");
        Group g = new Group("G01", "Familia Garcia");
        g.addMember(ana);
        g.addMember(pedro);
        new GroupFileStorage(file).save(List.of(g, new Group("G02", "Vacio")));
        assertEquals(List.of("G01;Familia Garcia;12345678Z,87654321X", "G02;Vacio;"), Files.readAllLines(file));
    }

    @Test
    void loadLinksMembersToTheGivenUsersAndSkipsUnknownOrMalformed() throws IOException {
        Path file = dir.resolve("groups.txt");
        Files.write(file, List.of("G01;Familia Garcia;12345678Z,00000000T,87654321X", "", "bad", "G02;Vacio;"));
        List<Group> groups = new GroupFileStorage(file).load(List.of(ana, pedro));
        assertEquals(2, groups.size());
        assertEquals(2, groups.get(0).getMembers().size());
        assertSame(ana, groups.get(0).getMembers().get(0));
        assertSame(pedro, groups.get(0).getMembers().get(1));
        assertTrue(groups.get(1).getMembers().isEmpty());
    }
}
