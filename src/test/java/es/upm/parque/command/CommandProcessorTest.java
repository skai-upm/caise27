package es.upm.parque.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.model.User;
import es.upm.parque.repository.UserRepository;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private UserRepository repo;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        processor = new CommandProcessor(repo);
    }

    private static String create(String dni, String email, String password, String card) {
        return "USER CREATE;Ana;Garcia Lopez;" + dni + ";" + email + ";" + password + ";612345678;" + card;
    }

    @Test
    void blankLinesProduceNoResponse() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
        assertNull(processor.process(null));
    }

    @Test
    void unknownCommandIsAnErrorAndKeepsState() {
        assertTrue(processor.process("FOO;bar").startsWith("ERROR"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertEquals(0, repo.size());
    }

    @Test
    void deleteWithMatchingEmailAndPasswordRemovesUser() {
        processor.process(ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertEquals(0, repo.size());
        assertTrue(processor.process(ANA).startsWith("OK"), "DNI and email reusable after deletion");
    }

    @Test
    void deleteWithWrongPasswordKeepsUser() {
        processor.process(ANA);
        String r = processor.process("USER DELETE;ana@correo.com;Otra1!");
        assertTrue(r.startsWith("ERROR") && r.contains("password"), r);
        assertEquals(1, repo.size());
        assertEquals("Clave1!", repo.findByDni("12345678Z").getPassword());
    }

    @Test
    void deleteUnknownEmailIsRejected() {
        processor.process(ANA);
        assertTrue(processor.process("USER DELETE;nadie@correo.com;Clave1!").startsWith("ERROR"));
        assertEquals(1, repo.size());
    }

    @Test
    void deleteWithWrongElementCountIsRejected() {
        processor.process(ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!;x").startsWith("ERROR"));
        assertEquals(1, repo.size());
    }

    @Test
    void validUserIsCreatedWithAllAttributes() {
        assertTrue(processor.process(ANA).startsWith("OK"));
        User u = repo.findByDni("12345678Z");
        assertEquals("Ana", u.getFirstName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getTelephone());
        assertEquals("1234567890123456", u.getCardNumber());
    }

    @Test
    void wrongNumberOfElementsIsRejected() {
        assertTrue(processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678").startsWith("ERROR"));
        assertTrue(processor.process(ANA + ";extra").startsWith("ERROR"));
        assertEquals(0, repo.size());
    }

    @Test
    void invalidFieldsAreRejectedNamingTheField() {
        String r;
        r = processor.process(create("12345678A", "ana@correo.com", "Clave1!", "1234567890123456"));
        assertTrue(r.startsWith("ERROR") && r.contains("DNI"), r);
        r = processor.process(create("12345678Z", "ana@correo.es", "Clave1!", "1234567890123456"));
        assertTrue(r.startsWith("ERROR") && r.contains("email"), r);
        r = processor.process(create("12345678Z", "ana@correo.com", "Clave12", "1234567890123456"));
        assertTrue(r.startsWith("ERROR") && r.contains("password"), r);
        r = processor.process(create("12345678Z", "ana@correo.com", "Clave1!", "123456789012345"));
        assertTrue(r.startsWith("ERROR") && r.contains("card"), r);
        assertEquals(0, repo.size());
    }

    @Test
    void duplicateDniIsRejected() {
        processor.process(ANA);
        String r = processor.process(create("12345678Z", "otra@correo.com", "Clave1!", "1234567890123456"));
        assertTrue(r.startsWith("ERROR") && r.contains("DNI"), r);
        assertEquals(1, repo.size());
        assertEquals("ana@correo.com", repo.findByDni("12345678Z").getEmail());
    }

    @Test
    void duplicateEmailIsRejected() {
        processor.process(ANA);
        String r = processor.process(create("87654321X", "ana@correo.com", "Clave1!", "1234567890123456"));
        assertTrue(r.startsWith("ERROR") && r.contains("email"), r);
        assertEquals(1, repo.size());
    }
}
