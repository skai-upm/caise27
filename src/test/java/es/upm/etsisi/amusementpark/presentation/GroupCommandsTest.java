package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.User;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupCommandsTest {

    private final ParkController controller = ParkController.getInstance();
    private String ana;
    private String pedro;
    private String id;

    private static String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new CommandProcessor().run(new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static int errors(String out) {
        int count = 0;
        for (int i = out.indexOf("ERROR"); i >= 0; i = out.indexOf("ERROR", i + 1)) {
            count++;
        }
        return count;
    }

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString();
        ana = "ana-" + suffix + "@correo.com";
        pedro = "pedro-" + suffix + "@correo.com";
        id = "G-" + suffix;
        controller.addUser(new User("DNI-" + ana, ana));
        controller.addUser(new User("DNI-" + pedro, pedro));
    }

    private Group group() {
        return controller.findGroup(id).orElseThrow();
    }

    @Test
    void createAddRemove() throws IOException {
        String out = run("GROUP CREATE;" + id + ";Familia Garcia\n"
                + "GROUP ADD;" + id + ";" + ana + "\n"
                + "GROUP ADD;" + id + ";" + pedro + "\n"
                + "GROUP REMOVE;" + id + ";" + pedro + "\n");
        assertEquals(0, errors(out), out);
        assertTrue(out.contains("GROUP CREATED"));
        assertTrue(out.contains("USER ADDED TO GROUP"));
        assertTrue(out.contains("USER REMOVED FROM GROUP"));
        assertEquals("Familia Garcia", group().getName());
        assertTrue(group().hasMember(ana));
        assertFalse(group().hasMember(pedro));
    }

    @Test
    void rejections() throws IOException {
        String out = run("GROUP CREATE;" + id + ";Familia Garcia\n"
                + "GROUP CREATE;" + id + ";Otro\n"
                + "GROUP CREATE;;Nombre\n"
                + "GROUP CREATE;X\n"
                + "GROUP ADD;" + id + ";" + ana + "\n"
                + "GROUP ADD;" + id + ";" + ana + "\n"
                + "GROUP ADD;" + id + ";nadie@correo.com\n"
                + "GROUP ADD;NO-SUCH-GROUP;" + ana + "\n"
                + "GROUP REMOVE;" + id + ";" + pedro + "\n"
                + "GROUP REMOVE;NO-SUCH-GROUP;" + ana + "\n");
        assertEquals(8, errors(out), out);
        assertEquals("Familia Garcia", group().getName());
        assertEquals(1, group().getMembers().size());
    }
}
