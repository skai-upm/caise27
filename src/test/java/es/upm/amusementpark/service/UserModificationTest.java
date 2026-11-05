package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.persistence.UserFileStorage;
import es.upm.amusementpark.repository.UserRepository;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserModificationTest {

    @TempDir
    Path dir;
    private UserRepository repo;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        repo = new UserRepository(new UserFileStorage(dir.resolve("users.txt")));
        processor = new CommandProcessor(new UserService(repo));
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
    }

    private User ana() {
        return repo.findByDni("12345678Z").orElseThrow();
    }

    @Test
    void enumHasExactlyTheModifiableFields() {
        assertEquals(6, UserField.values().length);
        assertTrue(UserField.fromName("DNI").isEmpty());
        assertTrue(UserField.fromName("AGE").isEmpty());
    }

    @Test
    void updatesEveryField() {
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;SURNAME;Garcia"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;CARD;9999888877776666"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Nueva2#;EMAIL;ana.nueva@correo.com"));
        User a = ana();
        assertEquals("Ana Maria", a.getFirstName());
        assertEquals("Garcia", a.getSurnames());
        assertEquals("699999999", a.getTelephone());
        assertEquals("9999888877776666", a.getCardNumber());
        assertEquals("Nueva2#", a.getPassword());
        assertEquals("ana.nueva@correo.com", a.getEmail());
    }

    @Test
    void dniUnknownFieldAndWrongCountRejected() {
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME").startsWith("ERROR"));
        assertEquals("12345678Z", ana().getDni());
        assertEquals("Ana", ana().getFirstName());
    }

    @Test
    void credentialsRequired() {
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Wrong1!;NAME;X").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;nadie@correo.com;Clave1!;NAME;X").startsWith("ERROR"));
        assertEquals("Ana", ana().getFirstName());
    }

    @Test
    void invalidValuesRejected() {
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.es").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave1!").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;CARD;123456789012345").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com").startsWith("ERROR"));
        User a = ana();
        assertEquals("ana@correo.com", a.getEmail());
        assertEquals("Clave1!", a.getPassword());
        assertEquals("1234567890123456", a.getCardNumber());
    }

    @Test
    void keepingOwnEmailAllowed() {
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.com"));
    }

    @Test
    void changeSurvivesRestart() {
        processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        UserRepository restarted = new UserRepository(new UserFileStorage(dir.resolve("users.txt")));
        assertEquals("699999999", restarted.findByDni("12345678Z").orElseThrow().getTelephone());
    }
}
