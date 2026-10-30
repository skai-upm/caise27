package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String COMMANDS = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "TICKET CREATE;T001;ana@correo.com;2099-01-01",
            "EXIT", "");

    @TempDir
    Path dir;

    private String run(String stdin, String... args) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), dir);
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private void assertNoDataFiles() {
        for (String name : new String[] {"users.txt", "groups.txt", "tickets.txt", "attractions.txt"}) {
            assertFalse(Files.exists(dir.resolve(name)), name);
        }
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void flyWithCommandFileLeavesNoData() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, COMMANDS);
        String output = run("", "--fly", input.toString());
        assertTrue(output.startsWith("USER CREATED"), output);
        assertTrue(output.contains("TICKET CREATED. PRICE: 40.00 EUR"), output);
        assertNoDataFiles();
    }

    @Test
    void flyInteractiveLeavesNoData() {
        String output = run(COMMANDS, "--fly");
        assertTrue(output.contains("GROUP CREATED"), output);
        assertNoDataFiles();
    }

    @Test
    void flyDoesNotTouchPermanentData() throws IOException {
        run(COMMANDS);
        String users = Files.readString(dir.resolve("users.txt"));
        String tickets = Files.readString(dir.resolve("tickets.txt"));
        String output = run("USER DELETE;ana@correo.com;Clave1!\nUSER CREATE;Pedro;Lopez Martin;87654321X;"
                + "pedro@correo.com;Otra2#;623456789;9876543210987654\nEXIT\n", "--fly");
        assertTrue(output.startsWith("ERROR: no user with email ana@correo.com"), output);
        assertEquals(users, Files.readString(dir.resolve("users.txt")));
        assertEquals(tickets, Files.readString(dir.resolve("tickets.txt")));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void flyWithMissingFileStillCleansUp() {
        String output = run("", "--fly", dir.resolve("nope.txt").toString());
        assertTrue(output.startsWith("ERROR: cannot read command file"), output);
        assertFalse(Files.exists(dir.resolve("tmp")));
    }

    @Test
    void normalModePersistsAcrossRuns() {
        run(COMMANDS);
        String output = run("TICKET CREATE;T001;ana@correo.com;2099-01-02\nGROUP ADD;G01;ana@correo.com\n"
                + "ATTRACTION LIST;A01\n");
        assertTrue(output.contains("ERROR: a ticket with identifier T001 already exists"), output);
        assertTrue(output.contains("ERROR: user ana@correo.com already belongs to group G01"), output);
        assertTrue(output.contains("NO USERS IN ATTRACTION A01"), output);
    }
}
