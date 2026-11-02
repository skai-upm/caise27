package es.upm.etsisi.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.amusementpark.park.DataPaths;
import es.upm.etsisi.amusementpark.park.Park;
import es.upm.etsisi.amusementpark.user.UserManager;

class CommandProcessorTest {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private UserManager users;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        Park park = Park.getInstance();
        park.load(DataPaths.in(dir));
        users = park.getUsers();
        processor = new CommandProcessor(park);
    }

    private String run(String line) {
        return processor.process(line).message();
    }

    @Test
    void createsUser() {
        assertEquals("USER CREATED", run(CREATE_ANA));
        assertEquals(1, users.getUsers().size());
    }

    @Test
    void rejectsInvalidOrDuplicateCreate() {
        assertEquals("USER CREATED", run(CREATE_ANA));
        assertTrue(run(CREATE_ANA).startsWith("ERROR:"));
        assertTrue(run("USER CREATE;Ana;Garcia;12345678A;x@correo.com;Clave1!;6;1234567890123456")
                .startsWith("ERROR:"));
        assertEquals(1, users.getUsers().size());
    }

    @Test
    void wrongElementCountIsRejected() {
        CommandResult result = processor.process("USER CREATE;Ana;Garcia Lopez");
        assertTrue(result.message().startsWith("ERROR:"));
        assertFalse(result.exit());
        assertTrue(users.getUsers().isEmpty());
        assertTrue(run("USER DELETE;ana@correo.com").startsWith("ERROR:"));
        assertTrue(run("USER UPDATE;ana@correo.com;Clave1!;NAME").startsWith("ERROR:"));
    }

    @Test
    void unknownCommandIsRejected() {
        CommandResult result = processor.process("FOO BAR");
        assertTrue(result.message().startsWith("ERROR:"));
        assertFalse(result.exit());
    }

    @Test
    void deletesUser() {
        run(CREATE_ANA);
        run(CREATE_PEDRO);
        assertTrue(run("USER DELETE;pedro@correo.com;Wrong1!").startsWith("ERROR:"));
        assertTrue(run("USER DELETE;nobody@correo.com;Otra2#").startsWith("ERROR:"));
        assertEquals("USER DELETED", run("USER DELETE;pedro@correo.com;Otra2#"));
        assertEquals(1, users.getUsers().size());
    }

    @Test
    void updatesUser() {
        run(CREATE_ANA);
        assertEquals("USER UPDATED", run("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("USER UPDATED", run("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertEquals("Ana Maria", users.findByEmail("ana.nueva@correo.com").getFirstName());
    }

    @Test
    void dniAndUnknownFieldsCannotBeUpdated() {
        run(CREATE_ANA);
        assertTrue(run("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").startsWith("ERROR:"));
        assertTrue(run("USER UPDATE;ana@correo.com;Clave1!;AGE;30").startsWith("ERROR:"));
        assertEquals("12345678Z", users.findByEmail("ana@correo.com").getDni());
    }
}
